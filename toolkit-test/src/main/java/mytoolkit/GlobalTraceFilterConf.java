package mytoolkit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import toolkit.enc.filter.HttpBodyEncFilter;
import toolkit.enc.properties.EncProperties;
import toolkit.traceid.global.GlobalTraceFilter;
import toolkit.traceid.properties.GlobalTraceProperties;

@Configuration
public class GlobalTraceFilterConf {

    @Bean
    public GlobalTraceProperties globalTraceProperties() {
        return new GlobalTraceProperties();
    }

    @Bean
    public FilterRegistrationBean<GlobalTraceFilter> globalTraceFilter(GlobalTraceProperties globalTraceProperties) {
        FilterRegistrationBean<GlobalTraceFilter> filter = new FilterRegistrationBean<>(new GlobalTraceFilter(globalTraceProperties), new ServletRegistrationBean[0]);
        filter.setOrder(0);
        return filter;
    }
}
