package xyz.erupt.upms.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author YuePeng
 * date 2019-10-31.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "erupt.upms", ignoreUnknownFields = false)
public class EruptUpmsConfig {

    //redis session时长
    private Integer expireTimeByLogin = 100;

}
