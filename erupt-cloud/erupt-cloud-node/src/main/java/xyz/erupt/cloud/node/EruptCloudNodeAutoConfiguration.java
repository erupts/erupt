package xyz.erupt.cloud.node;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import xyz.erupt.cloud.common.http.CloudHttp;


/**
 * @author YuePeng
 * date 2021/12/16 00:15
 */
@Configuration
@ComponentScan
@EnableConfigurationProperties
public class EruptCloudNodeAutoConfiguration {

    // Shared client for node -> server calls; no response timeout, matching the previous behaviour
    @Bean
    public RestClient serverRestClient() {
        return CloudHttp.client(null);
    }

}
