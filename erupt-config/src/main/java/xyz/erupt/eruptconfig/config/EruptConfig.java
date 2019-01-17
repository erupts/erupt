package xyz.erupt.eruptconfig.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Created by liyuepeng on 2019-01-17.
 */
@Data
@Configuration
@PropertySource("classpath:erupt.properties")
public class EruptConfig {

    @Value("${host}")
    private String host;

    @Value("${port}")
    private int port;

}
