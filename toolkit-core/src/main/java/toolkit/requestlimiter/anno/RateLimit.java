package toolkit.requestlimiter.anno;

import org.springframework.stereotype.Indexed;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 *  限流注解
 * @author zengyilun
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Indexed
public @interface RateLimit {
    /**
     * 是否启用限流
     */
    boolean enabled() default true;
    /**
     * 限流总量
     */
    int capacity() default 0;
    /**
     * 每次补充的令牌数量
     */
    int refillTokens() default 0;
     /**
      * 补充令牌的时间间隔
      */
    long refillDuration() default 0;
     /**
      * 补充令牌的时间单位
      */
    TimeUnit refillDurationUnit() default TimeUnit.MINUTES;
}
