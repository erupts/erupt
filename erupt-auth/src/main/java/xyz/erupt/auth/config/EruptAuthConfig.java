package xyz.erupt.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author liyuepeng
 * @date 2019-10-31.
 */
@Data
@Component
@ConfigurationProperties(prefix = "erupt-auth", ignoreUnknownFields = false)
public class EruptAuthConfig {

    private Integer expireTimeByLogin = 100;
}
