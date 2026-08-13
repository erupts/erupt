package xyz.erupt.notion.prop;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Notion integration credentials, bound from {@code erupt.notion.*} in the
 * application configuration. Secrets stay out of source and annotations.
 *
 * @author YuePeng
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "erupt.notion")
public class EruptNotionProperties {

    /**
     * Internal integration token (secret_...), sent as a Bearer token.
     */
    private String token;

    /**
     * Notion API version, sent in the {@code Notion-Version} header.
     */
    private String version = "2022-06-28";

    /**
     * API base URL; override for a proxy.
     */
    private String baseUrl = "https://api.notion.com";

}
