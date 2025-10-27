package toolkit.requestlimiter.anno;

import org.springframework.context.annotation.Import;
import toolkit.requestlimiter.config.RequestLimiterConfig;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(RequestLimiterConfig.class)
public @interface EnableRequestLimiter {
}