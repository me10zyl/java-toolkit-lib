package toolkit.utils;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.*;


@Slf4j
public class ExecutorUtil {
    private final ConcurrentHashMap<String, ExecutorService> cache = new ConcurrentHashMap<>();

    public synchronized Future<?> doAsync(String name, int threadSize, Runnable runnable){
        ExecutorService executorService = cache.get(name);
        if(executorService == null){
            log.info("创建线程池SIZE={}", threadSize);
            executorService = Executors.newFixedThreadPool(threadSize);
            cache.put(name, executorService);
        }
        String globalTraceId = MDC.get("globalTraceId");
        return executorService.submit(() -> {
            try {
                MDC.put("globalTraceId", globalTraceId);
                runnable.run();
            } catch (Exception e) {
                log.error("线程执行出错", e);
            }
        });
    }

    @PreDestroy
    public void preDestroy(){
        log.info("请再等等JOB的线程池销毁");
        cache.forEach((key, executorService) -> {
            executorService.shutdown();
        });
        cache.forEach((key, executorService)->{
            try {
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException ex) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        });

    }


}