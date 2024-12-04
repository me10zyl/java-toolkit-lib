package toolkit.logger;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.p6spy.engine.logging.Category;
import com.p6spy.engine.spy.appender.Slf4JLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class P6SpyLogger extends Slf4JLogger {
    private Logger log = LoggerFactory.getLogger("p6spy");

    public void logText(String text) {
        this.log.info(text);
    }

    @Override
    public void logSQL(int connectionId, String now, long elapsed, Category category, String prepared, String sql, String url) {
        if(StringUtils.isNotBlank(sql) && !"SELECT 1".equals(sql) && !sql.contains("lcs_life_cycle") && !sql.contains("tsk_system_task")
        && !sql.contains("tsk_system_exec_order")) {
            String msg1 = "花费时间: " + elapsed + " ms" + " 连接信息: connection" + connectionId;
            String msg2 = "执行SQL: " + sql.replaceAll("[\\s]+", " ");
            if (Category.ERROR.equals(category)) {
                this.log.error(msg1);
                this.log.error(msg2);
            } else if (Category.WARN.equals(category)) {
                this.log.warn(msg1);
                this.log.warn(msg2);
            } else if (Category.DEBUG.equals(category)) {
                this.log.debug(msg1);
                this.log.debug(msg2);
            } else {
                this.log.info(msg1);
                this.log.info(msg2);
            }
        }
    }
}