package toolkit.utils;

import java.util.concurrent.TimeUnit;
import java.time.temporal.TemporalUnit;
import java.time.temporal.ChronoUnit;

public class TimeUnitConverter {

    /**
     * Java 8 中手动将 TimeUnit 转换为 TemporalUnit（ChronoUnit）
     * @param timeUnit 旧时间单位
     * @return 对应的 TemporalUnit 实例
     * @throws IllegalArgumentException 不支持的时间单位
     */
    public static TemporalUnit toTemporalUnit(TimeUnit timeUnit) {
        if (timeUnit == null) {
            throw new IllegalArgumentException("TimeUnit must not be null");
        }
        switch (timeUnit) {
            case NANOSECONDS:
                return ChronoUnit.NANOS;
            case MICROSECONDS:
                return ChronoUnit.MICROS;
            case MILLISECONDS:
                return ChronoUnit.MILLIS;
            case SECONDS:
                return ChronoUnit.SECONDS;
            case MINUTES:
                return ChronoUnit.MINUTES;
            case HOURS:
                return ChronoUnit.HOURS;
            case DAYS:
                return ChronoUnit.DAYS;
            default:
                throw new IllegalArgumentException("Unsupported TimeUnit: " + timeUnit);
        }
    }

    // 测试示例
    public static void main(String[] args) {
        TimeUnit timeUnit = TimeUnit.HOURS;
        TemporalUnit temporalUnit = toTemporalUnit(timeUnit);
        System.out.println(temporalUnit); // 输出：Hours
    }
}