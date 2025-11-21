package toolkit.scanner;

import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.NativeWebRequest;
import toolkit.scanner.dao.JobScannerMapper;
import toolkit.scanner.entity.JobScannerDO;
import toolkit.traceid.Constants;
import toolkit.traceid.MDCUtil;
import toolkit.utils.ContextHolder;
import toolkit.utils.IpUtil;
import toolkit.utils.StackTraceUtil;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;

@Component
@Slf4j
public class JobScannerDispatcher {
    @Autowired
    private JobScannerMapper jobScannerMapper;
    @Autowired
    private List<JobScanner> jobScanners;
    private ExecutorService executorService = new ThreadPoolExecutor(
            20, 20, 0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(100), new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            JobScannerTask jobScannerTask = (JobScannerTask) r;
            return new Thread(r, "jobScannerDispatcher-" + jobScannerTask.jobScannerDO.getJobName());
        }
    });

    @PreDestroy
    public void destroy() {
        if (!executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
        }
    }

    class JobScannerTask implements Runnable {
        private final JobScanner jobScanner;
        private final JobScannerDO jobScannerDO;

        public JobScannerTask(JobScanner jobScanner, JobScannerDO jobScannerDO) {
            this.jobScanner = jobScanner;
            this.jobScannerDO = jobScannerDO;
        }

        @Override
        public void run() {
            List records = null;
            try {
                MDCUtil.putMDCGlobalTraceId();
                jobScannerDO.setLastExecuteTime(LocalDateTime.now());
                jobScannerDO.setLastExecuteTraceId(MDC.get(Constants.MDC_GLOBAL_TRACE_ID));
                jobScannerDO.setLastExecuteIp(IpUtil.getLocalIP());
                records = jobScanner.selectRecordsWithNextExecuteTimeAndCount(jobScannerDO.getMaxTimes());
                jobScanner.handleRecords(records);
                jobScannerDO.setLastExecuteResult("success");
            } catch (Exception e) {
                log.error("jobScannerDispatcher error, jobName: {}", jobScanner.getJobName(), e);
                jobScannerDO.setLastExecuteResult(StackTraceUtil.getStackTrace(e));
                if (records != null && !records.isEmpty()) {
                    ContextHolder.getContext().getBean(JobScannerDispatcher.class)
                            .updateRecordNextExecuteTime(jobScanner, jobScannerDO, records);

                }
            } finally {
                jobScannerMapper.updateById(jobScannerDO);
                MDCUtil.removeMDCGlobalTraceId();
            }
        }
    }

    @XxlJob("jobScannerDispatcher")
    public void dispatch() {
        List<JobScannerDO> jobScannerDOList = jobScannerMapper.selectWaitToScan();
        for (JobScannerDO jobScannerDO : jobScannerDOList) {
            jobScanners.stream().filter(jobScanner ->
                            jobScanner.getJobName().equals(jobScannerDO.getJobName()))
                    .findFirst()
                    .ifPresent(jobScanner -> {
                        executorService.submit(new JobScannerTask(jobScanner, jobScannerDO));
                    });
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateRecordNextExecuteTime(JobScanner jobScanner, JobScannerDO jobScannerDO, List records) {
        jobScanner.updateNextExecuteTime(records,
                LocalDateTime.now().plusSeconds(jobScannerDO.getExecuteInterval()));
        for (Object record : records) {
            jobScanner.updateCount(record);
        }
    }
}
