package toolkit.scanner;

import java.time.LocalDateTime;
import java.util.List;

public interface JobScanner<T> {

    String getJobName();

    List<T> selectRecordsWithNextExecuteTimeAndCount(Integer maxTimes);

    void handleRecords(List<T> records);

    void updateNextExecuteTime(List<T> records, LocalDateTime nextExecuteTime);

    void updateCount(Object record);
}
