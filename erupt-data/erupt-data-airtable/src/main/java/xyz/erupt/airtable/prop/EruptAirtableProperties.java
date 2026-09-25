package xyz.erupt.airtable.prop;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Airtable credentials, bound from {@code erupt.airtable.*} in the application
 * configuration. Secrets stay out of source and annotations. Registered as a
 * bean via {@code @EnableConfigurationProperties} in the auto-configuration, so
 * no stereotype annotation here.
 *
 * @author YuePeng
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "erupt.airtable")
public class EruptAirtableProperties {

    /**
     * Personal access token (or OAuth access token) with {@code data.records:read}
     * / {@code data.records:write} scopes on the bound bases.
     */
    private String token;

    /**
     * REST base URL; override for a proxy.
     */
    private String baseUrl = "https://api.airtable.com";

}
