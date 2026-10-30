package xyz.erupt.share.prop;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author YuePeng
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "erupt.share", ignoreUnknownFields = false)
public class EruptShareProp {

    // Longest life of a session opened from a link, in minutes; the link's own expiry cuts it shorter
    private int sessionMinutes = 120;

    // Failed exchanges one client address may make within the window before it is refused
    private int maxAttempts = 20;

    // Length of the attempt window, in minutes
    private int attemptWindowMinutes = 10;

}
