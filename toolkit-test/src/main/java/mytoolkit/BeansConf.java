package mytoolkit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import toolkit.requestlimiter.config.RequestLimterConfig;

@Configuration
public class BeansConf {

    @Bean
    public RequestLimterConfig requestLimterConfig(){
        return new RequestLimterConfig();
    }
}
