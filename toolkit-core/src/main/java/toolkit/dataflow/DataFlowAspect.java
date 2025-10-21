package toolkit.dataflow;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import toolkit.dataflow.util.DFHolder;

@Component
@Aspect
@Slf4j
public class DataFlowAspect {

    @Around("@annotation(toolkit.dataflow.anno.DataFlow)")
    public Object aroundDataFlow(ProceedingJoinPoint point) throws Throwable {
        Object proceed = null;
        try {
            DFHolder.init();
            proceed = point.proceed();
        }finally {
            try {
                DFHolder.release();
            }catch (Exception e){
                log.error("DataFlowHolder release error", e);
            }
        }
        return proceed;
    }
}
