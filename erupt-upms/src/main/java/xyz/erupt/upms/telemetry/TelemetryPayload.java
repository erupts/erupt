package xyz.erupt.upms.telemetry;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Anonymous instance profile sent to the telemetry endpoint.
 * <p>
 * Every field here must be non-identifying: no hostname, no application name, no JDBC url,
 * no user data. Anything that could tie a payload back to a person or a business does not
 * belong in this class.
 *
 * @author YuePeng
 * date 2026/09/02
 */
@Getter
@Setter
@Builder
public class TelemetryPayload {

    // Payload layout version, bumped whenever fields are added or removed
    private int schema;

    // 'boot' or 'heartbeat'
    private String eventType;

    // Random UUID persisted under .erupt, used only to deduplicate instances
    private String instanceId;

    // Version distribution, the single most important signal
    private String eruptVersion;

    // Which modules are installed, tells which ones deserve investment
    private List<String> modules;

    // Number of registered @Erupt classes, separates real deployments from tutorial demos
    private int eruptCount;

    // Decides when JDK baselines can be raised
    private String javaVersion;

    private String javaVendor;

    // Share of domestic / ARM deployments
    private String os;

    // Linux distribution id, os.name is always 'Linux' and cannot show domestic OS share
    private String osDistro;

    private String arch;

    // Whether the instance runs inside a container, decides how much the docker image is worth
    private boolean containerized;

    // Guides cross-database SQL compatibility priorities
    private String dbType;

    private String dbVersion;

    // Decides how long Spring Boot 2.x has to stay supported
    private String springBootVersion;

    // Configured default locale, decides which i18n translations deserve investment
    private String locale;

    // Coarse regional distribution, cleaner than keeping IP addresses
    private String timezone;

}
