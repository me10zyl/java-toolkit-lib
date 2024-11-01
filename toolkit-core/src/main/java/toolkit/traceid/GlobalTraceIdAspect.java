package toolkit.traceid;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.MDC;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.DynamicMethodMatcherPointcut;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
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
        MDC.put("globalTraceId", UUID.randomUUID().toString().substring(0, 8));
        MDC.put("methodName", methodName);
    }


    @Override
    public Pointcut getPointcut() {
        return new DynamicMethodMatcherPointcut() {

            @Override
            public ClassFilter getClassFilter() {
                return aClass -> aClass.getName().startsWith(GlobalTraceIdAspect.this.aopPrefix);
            }

            @Override
            public boolean matches(Method method, Class<?> aClass, Object... objects) {
                return true;
            }
        };
    }

    @Override
    public Advice getAdvice() {
        return (MethodInterceptor) methodInvocation -> {
            before(methodInvocation.getMethod().getName());
            return methodInvocation.proceed();
        };
    }
}
