package toolkit.distribution;



import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.env.Environment;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import toolkit.distribution.anno.DistributeLock;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Slf4j
@Aspect
@Configuration
public class RedisLockAspect {

    /**
     * spel 解析器
     */
    ExpressionParser parser = new SpelExpressionParser();
    LocalVariableTableParameterNameDiscoverer discoverer = new LocalVariableTableParameterNameDiscoverer();

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private Environment environment;

    @PostConstruct
    public void init() {
        log.info("初始化分布式锁...");
    }

    @Around("@annotation(lock)")
    public Object invoked(ProceedingJoinPoint pjp, DistributeLock lock) throws Throwable {
        Object[] args = pjp.getArgs();
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        String[] params = discoverer.getParameterNames(method);
        String env = Arrays.stream(environment.getActiveProfiles()).collect(Collectors.joining(","));
        EvaluationContext context = new StandardEvaluationContext();
        context.setVariable("method", method);
        context.setVariable("env", env);
        if (params != null && params.length > 0) {
            for (int len = 0; len < params.length; len++) {
                context.setVariable(params[len], args[len]);
            }
        }
        //key处理
        String keySpel = lock.key();
        String key = null;
        if (StrUtil.isBlank(keySpel)) {
            key = environment.getProperty("spring.application.name") + ":" + env + "-" + method.getDeclaringClass().getName() + "#" + method.getName();
        } else {
            Expression keyExpression = parser.parseExpression(keySpel);
            key = keyExpression.getValue(context, String.class);
        }

        //是否加锁
        String unlessSpel = lock.unless();
        Boolean unless = false;
        if (!StrUtil.isBlank(unlessSpel)) {
            Expression unlessExpression = parser.parseExpression(unlessSpel);
            unless = unlessExpression.getValue(context, Boolean.class);
        }

        //超时时间
        long timeout = lock.timeout();
        TimeUnit timeUnit = lock.timeUnit();

        log.info("————————————————————————分布式锁{}开始————————————————————————", key);
        long start = System.currentTimeMillis();
        //加锁
//        boolean lockBol = true;
        RLock rLock = null;
        if (!Boolean.TRUE.equals(unless)) {
            log.info("尝试获取分布式锁...");
            rLock = redissonClient.getLock(key);
            rLock.lock();
            log.info("分布式锁定完成.");
            //lockBol = redisLockService.tryLock(key, timeout, timeUnit);
        }
        //为获得锁
//        if (!lockBol) {
//            throw new ServiceException(StrUtil.isNotBlank(lock.message()) ? lock.message() : "系统繁忙请稍后再试");
//        }
        // 获取锁成功
        try {
            return pjp.proceed();
        } finally {
            if (!Boolean.TRUE.equals(unless) && rLock != null) {
                // 释放锁。
                log.info("准备释放锁...");
                rLock.unlock();
                log.info("执行释放锁.");
            }
            log.info("————————————————————————分布式锁{}执行完成,执行时间{}ms————————————————————————", key,
                    (System.currentTimeMillis() - start));
        }
    }
}
