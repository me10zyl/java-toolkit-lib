package toolkit.traceid.aspect;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.stereotype.Component;
import toolkit.traceid.MDCUtil;
import toolkit.traceid.PrefixMatchJoinPoint;

import java.util.UUID;


/**
 * 全球跟踪id切面
 *
 * @author zyl
 * @date 2022/06/02
 */
@Aspect
@Component
public class GlobalTraceIdAspect extends AbstractPointcutAdvisor {

    private final String aopPrefix;

    public GlobalTraceIdAspect(String aopPrefix) {
        this.aopPrefix = aopPrefix;
    }

    public void before(String methodName) throws Throwable {
        MDCUtil.putMDCGlobalTraceId();
        MDC.put("methodName", methodName);
    }


    @Override
    public Pointcut getPointcut() {
         return new PrefixMatchJoinPoint(aopPrefix);
    }

    @Override
    public Advice getAdvice() {
        return (MethodInterceptor) methodInvocation -> {
            before(methodInvocation.getMethod().getName());
            return methodInvocation.proceed();
        };
    }
}
