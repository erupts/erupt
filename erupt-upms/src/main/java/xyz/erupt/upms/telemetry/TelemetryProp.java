package xyz.erupt.upms.telemetry;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration for {@link EruptTelemetry}.
 * <p>
 * Bound to {@code erupt.telemetry} rather than {@code erupt.upms.telemetry}: telemetry describes
 * the erupt install as a whole, upms merely happens to host it. Declaring it here instead of in
 * erupt-core's EruptProp keeps the configuration next to the code that reads it.
 *
 * @author YuePeng
 * date 2026/09/02
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "erupt.telemetry")
public class TelemetryProp {

    public static final String DEFAULT_ENDPOINT = "https://telemetry.erupt.xyz/v1/ping";

    // Whether to report anonymous usage statistics
    private boolean enabled = true;

    // Reporting endpoint, override it to point at a self-hosted collector
    private String endpoint = DEFAULT_ENDPOINT;

}
