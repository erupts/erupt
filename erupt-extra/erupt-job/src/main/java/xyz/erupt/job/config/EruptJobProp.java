package xyz.erupt.job.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author YuePeng
 * date 2021/3/28 19:28
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "erupt.job", ignoreUnknownFields = false)
public class EruptJobProp {

    private boolean enable = true;

}
