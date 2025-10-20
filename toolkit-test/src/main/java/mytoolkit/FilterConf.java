package mytoolkit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import toolkit.enc.filter.HttpBodyEncFilter;
import toolkit.enc.properties.EncProperties;
import toolkit.enc.advice.RestReturnWrapperHandler;

@Configuration
public class FilterConf {

    private final String[] excludePatterns = {};
    @Bean
    public RestReturnWrapperHandler restReturnWrapperHandler(EncProperties encProperties, ObjectMapper objectMapper, Environment environment) {
        return new RestReturnWrapperHandler(encProperties, objectMapper, environment, new String[]{"test", "dev", "fat"}, excludePatterns);
    }

    @Bean
    public EncProperties encProperties() {
        return new EncProperties();
    }

    @Bean
    public FilterRegistrationBean<HttpBodyEncFilter> decryptionFilter(EncProperties encProperties, ObjectMapper objectMapper, Environment environment) {
        FilterRegistrationBean<HttpBodyEncFilter> filter = new FilterRegistrationBean<>(new HttpBodyEncFilter(encProperties, objectMapper, environment, new String[]{"test", "dev", "fat"}, excludePatterns), new ServletRegistrationBean[0]);
        filter.setOrder(0);
        return filter;
    }
}