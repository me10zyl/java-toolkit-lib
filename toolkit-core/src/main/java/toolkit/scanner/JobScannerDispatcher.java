package toolkit.scanner;

import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import toolkit.scanner.dao.JobScannerMapper;
import toolkit.scanner.entity.JobScannerDO;
import toolkit.traceid.Constants;
import toolkit.traceid.MDCUtil;
import toolkit.utils.ContextHolder;
import toolkit.utils.IpUtil;
import toolkit.utils.StackTraceUtil;

import javax.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;

//DDL:-- auto-generated definition
//create table job_scanner
//(
//    job_id                bigint auto_increment
//        primary key,
//    job_name              varchar(50)                        not null comment 'job名称',
//    execute_interval      int      default ((60 * 5))        not null comment '执行间隔（秒）',
//    next_execute_time     datetime                           null comment '下次执行时间',
//    last_execute_time     datetime                           null comment 'job最后执行时间',
//    last_execute_result   text                               null comment '最后执行结果',
//    last_execute_trace_id varchar(32)                        null comment '最够执行的traceid',
//    last_execute_ip       varchar(20)                        null comment '最后执行的ip',
//    max_times             int      default 5                 not null comment '最大执行次数',
//    job_desc              varchar(100)                       null comment 'job描述',
//    created_at            datetime default CURRENT_TIMESTAMP not null comment '创建时间',
//    update_time           datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
//);


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
