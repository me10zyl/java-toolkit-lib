package toolkit.logger;


import org.apache.ibatis.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import toolkit.properties.MybatisLogProperties;
import toolkit.utils.ContextHolder;


public class MybatisSqlLogImpl implements Log {

    private Logger log;
    private MybatisLogProperties mybatisLogProperties;

    public MybatisSqlLogImpl(String clazz) {
        log = LoggerFactory.getLogger(clazz);
    }

    public boolean isDebugEnabled() {
        MybatisLogProperties mybatisProperties = getMybatisProperties();
        if(mybatisProperties == null){
            return this.log.isDebugEnabled();
        }
        return mybatisProperties.isDebug();
    }

    private MybatisLogProperties getMybatisProperties() {
        if(mybatisLogProperties == null) {
            ApplicationContext context = ContextHolder.getContext();
            if(context == null){
                return null;
            }
            mybatisLogProperties = context.getBean(MybatisLogProperties.class);
        }
        return mybatisLogProperties;
    }

    public boolean isTraceEnabled() {
        MybatisLogProperties mybatisProperties = getMybatisProperties();
        if(mybatisProperties == null){
            return this.log.isDebugEnabled();
        }
        return mybatisProperties.isTrace();
    }

    public void error(String s, Throwable e) {
        this.log.error(s, e);
    }

    public void error(String s) {
        this.log.error(s);
    }

    public void debug(String s) {
        /*if(s.startsWith("==>")){
            String str = "================debug================";
            s = str + "\n" + s + str;
        }*/
        this.log.info(s);
    }

    public void trace(String s) {
        this.log.info(s);
    }

    public void warn(String s) {
        this.log.warn(s);
    }
}
