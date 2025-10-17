package mytoolkit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import toolkit.enc.filter.HttpBodyEncFilter;
import toolkit.enc.properties.EncProperties;
import toolkit.wrapper.RestReturnWrapperHandler;

@Configuration
public class FilterConf {

    @Bean
    public RestReturnWrapperHandler restReturnWrapperHandler(EncProperties encProperties, ObjectMapper objectMapper){
        return new RestReturnWrapperHandler(encProperties, objectMapper, new String[]{
                "/test/gen"
        });
    }

    @Bean
    public EncProperties encProperties(){
        return new EncProperties();
    }

    @Bean
    public FilterRegistrationBean<HttpBodyEncFilter> decryptionFilter(EncProperties encProperties, ObjectMapper objectMapper) {
        FilterRegistrationBean<HttpBodyEncFilter> filter = new FilterRegistrationBean<>(new HttpBodyEncFilter(encProperties, new String[]{"/test/gen"}, ()->{return false;}, objectMapper), new ServletRegistrationBean[0]  );
        filter.setOrder(0);
        return filter;
    }
}