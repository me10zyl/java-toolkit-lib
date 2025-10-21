package toolkit.distribution.anno;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributeLock {
    /**
     * 加锁的唯一值
     *
     * @return
     */
    String key() default "";

    /**
     * 为真时不加锁
     *
     * @return
     */
    String unless() default "";

    /**
     * 超时时间
     *
     * @return
     */
    long timeout() default 600;

    /**
     * 超时单位
     *
     * @return
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 获取锁失败时报错信息
     */
    String message() default "";
}