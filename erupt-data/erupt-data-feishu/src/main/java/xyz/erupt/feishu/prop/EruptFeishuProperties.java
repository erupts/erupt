package xyz.erupt.feishu.prop;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Feishu open-platform credentials, bound from {@code erupt.feishu.*} in the
 * application configuration. Secrets stay out of source and annotations.
 *
 * @author YuePeng
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "erupt.feishu")
public class EruptFeishuProperties {

    /**
     * Custom-app id (app_id) used to obtain a tenant access token.
     */
    private String appId;

    /**
     * Custom-app secret (app_secret).
     */
    private String appSecret;

    /**
     * Open-platform base URL; override for Lark (feishu international) or a proxy.
     */
    private String baseUrl = "https://open.feishu.cn";

}
