package mytoolkit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qxnw.scyc.common.aop.controlleradvice.RestReturnWrapperHandler;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import toolkit.enc.filter.HttpBodyEncFilter;
import toolkit.enc.properties.EncProperties;

@Configuration
public class FilterConf {

    @Bean
    public com.qxnw.scyc.common.aop.controlleradvice.RestReturnWrapperHandler restReturnWrapperHandler(EncProperties encProperties, ObjectMapper objectMapper){
        return new RestReturnWrapperHandler(encProperties, objectMapper);
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