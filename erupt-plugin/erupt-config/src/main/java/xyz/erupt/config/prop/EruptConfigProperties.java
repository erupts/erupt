package xyz.erupt.config.prop;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * <pre>
 * erupt:
 *   config:
 *     refresh-seconds: 30    # how often other instances' changes are picked up; 0 turns polling off
 * </pre>
 * Which profiles an instance reads comes from {@code spring.profiles.active}, nothing of its own.
 *
 * @author YuePeng
 */
@Getter
@Setter
@Component
@ConfigurationProperties(EruptConfigProperties.PREFIX)
public class EruptConfigProperties {

    public static final String PREFIX = "erupt.config";

    // Seconds between polls for another instance's change; 0 (the default) does not poll. Only a
    // cluster without erupt.redis-session needs it: with Redis sessions changes are broadcast
    private int refreshSeconds = 0;

}
