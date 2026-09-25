package xyz.erupt.dingtalk.prop;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * DingTalk open-platform credentials, bound from {@code erupt.dingtalk.*} in the
 * application configuration. Secrets stay out of source and annotations.
 * Registered as a bean via {@code @EnableConfigurationProperties} in the
 * auto-configuration, so no stereotype annotation here.
 *
 * @author YuePeng
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "erupt.dingtalk")
public class EruptDingTalkProperties {

    /**
     * Client ID of the enterprise-internal app (shown as AppKey in older consoles),
     * used to obtain an access token.
     */
    private String clientId;

    /**
     * Client Secret of the app (shown as AppSecret in older consoles).
     */
    private String clientSecret;

    /**
     * Union id of the user Notable calls are made on behalf of; the Notable API
     * requires one on every request. Can be overridden per model on the annotation.
     */
    private String operatorId;

    /**
     * Open-platform base URL; override for a proxy.
     */
    private String baseUrl = "https://api.dingtalk.com";

}
