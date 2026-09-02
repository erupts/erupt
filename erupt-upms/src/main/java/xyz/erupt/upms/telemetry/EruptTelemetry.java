package xyz.erupt.upms.telemetry;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.prop.EruptProp;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.util.EruptInformation;

import javax.sql.DataSource;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Anonymous usage telemetry: reports which erupt version, modules, JDK, OS, database and
 * Spring Boot version an instance runs on, so support and compatibility work can be
 * prioritised with real numbers instead of guesses.
 * <p>
 * <b>Why this lives in erupt-upms and not erupt-core:</b> erupt-cloud-node depends on erupt-core
 * but never on erupt-upms, so hosting the collector here is what keeps cloud nodes from reporting
 * at all. That exclusion is the whole reason for the placement, so do not move this class down
 * into erupt-core, and do not add an erupt-upms dependency to erupt-cloud-node without deciding
 * what should happen to node telemetry first. The module list it reports still comes from
 * erupt-core via {@link EruptCoreService}, which upms depends on.
 * <p>
 * Design rules:
 * <ul>
 *     <li>Nothing identifying is collected, see {@link TelemetryPayload}.</li>
 *     <li>Never blocks or breaks the host application: daemon thread, short timeouts, silent failure.</li>
 *     <li>Opt-out is honoured through {@code erupt.telemetry.enabled=false} or the
 *     {@code ERUPT_TELEMETRY_DISABLED} environment variable, and CI environments are skipped.
 *     The collection is disclosed in the project README, not at runtime, so startup logs stay quiet.</li>
 * </ul>
 *
 * @author YuePeng
 * date 2026/09/02
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "erupt.telemetry.enabled", havingValue = "true", matchIfMissing = true)
public class EruptTelemetry implements ApplicationListener<ApplicationReadyEvent>, DisposableBean {

    // Plain Gson on purpose: no MetaContext coupling, and null fields are dropped instead of sent
    private static final Gson GSON = new Gson();

    private static final int SCHEMA_VERSION = 2;

    // Let the startup sequence finish before touching the network or the connection pool
    private static final long BOOT_DELAY_SECONDS = 15;

    private static final long HEARTBEAT_HOURS = 24;

    private static final int TIMEOUT_MILLIS = 3000;

    // The endpoint may answer with an advisory, keep both the read and the log line bounded
    private static final int MAX_RESPONSE_BYTES = 4096;

    private static final int MAX_ADVISORY_CHARS = 300;

    private static final String INSTANCE_ID_FILE = "telemetry.id";

    // os-release lives in /etc, /usr/lib is the fallback mandated by the freedesktop spec
    private static final String[] OS_RELEASE_FILES = {"/etc/os-release", "/usr/lib/os-release"};

    // Distribution ids are short slugs such as 'kylin' / 'uos' / 'openeuler', cap them anyway
    private static final int MAX_OS_DISTRO_CHARS = 32;

    private final EruptProp eruptProp;

    private final TelemetryProp telemetryProp;

    private final ObjectProvider<DataSource> dataSourceProvider;

    private final AtomicBoolean started = new AtomicBoolean();

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "erupt-telemetry");
        // must stay daemon, telemetry may never hold the JVM open
        thread.setDaemon(true);
        return thread;
    });

    public EruptTelemetry(EruptProp eruptProp, TelemetryProp telemetryProp,
                          ObjectProvider<DataSource> dataSourceProvider) {
        this.eruptProp = eruptProp;
        this.telemetryProp = telemetryProp;
        this.dataSourceProvider = dataSourceProvider;
    }

    @Override
    public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
        // a context refresh can republish this event, report once per JVM
        if (!started.compareAndSet(false, true)) return;
        if (disabledByEnv()) return;
        String instanceId = resolveInstanceId();
        if (null == instanceId) return;
        scheduler.schedule(() -> send(instanceId, "boot"), BOOT_DELAY_SECONDS, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(() -> send(instanceId, "heartbeat"),
                HEARTBEAT_HOURS, HEARTBEAT_HOURS, TimeUnit.HOURS);
    }

    @Override
    public void destroy() {
        scheduler.shutdownNow();
    }

    // Environment level opt-out, evaluated on top of the erupt.telemetry.enabled property
    private boolean disabledByEnv() {
        String disabled = System.getenv("ERUPT_TELEMETRY_DISABLED");
        return "1".equals(disabled) || "true".equalsIgnoreCase(disabled)
                // build agents would flood the stats with throwaway instances
                || StringUtils.isNotBlank(System.getenv("CI"));
    }

    /**
     * Read the instance id from {@code .erupt/telemetry.id}, creating it on first run.
     * Persisting it is what makes deduplication and active-instance counts possible, if the
     * file cannot be written telemetry is skipped rather than reporting a fresh identity on
     * every restart.
     */
    private String resolveInstanceId() {
        File dir = new File(EruptConst.ERUPT_DIR_PATH);
        File file = new File(dir, INSTANCE_ID_FILE);
        try {
            if (file.exists()) {
                String id = FileUtils.readFileToString(file, StandardCharsets.UTF_8).trim();
                if (StringUtils.isNotBlank(id)) return id;
            }
            if (!dir.exists() && !dir.mkdirs()) return null;
            String id = UUID.randomUUID().toString();
            FileUtils.writeStringToFile(file, id, StandardCharsets.UTF_8);
            return id;
        } catch (Exception e) {
            log.debug("telemetry instance id unavailable", e);
            return null;
        }
    }

    private void send(String instanceId, String eventType) {
        HttpURLConnection conn = null;
        try {
            String json = GSON.toJson(this.buildPayload(instanceId, eventType));
            conn = (HttpURLConnection) URI.create(this.endpoint()).toURL().openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(TIMEOUT_MILLIS);
            conn.setReadTimeout(TIMEOUT_MILLIS);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("User-Agent", "erupt/" + EruptInformation.getEruptVersion());
            conn.setDoOutput(true);
            try (OutputStream out = conn.getOutputStream()) {
                out.write(json.getBytes(StandardCharsets.UTF_8));
            }
            if (HttpURLConnection.HTTP_OK == conn.getResponseCode()) {
                this.handleAdvisory(this.readBody(conn));
            }
        } catch (Throwable ignored) {
            // offline, intranet-only and firewalled deployments are all normal, stay quiet
        } finally {
            if (null != conn) conn.disconnect();
        }
    }

    private String endpoint() {
        String endpoint = telemetryProp.getEndpoint();
        return StringUtils.isBlank(endpoint) ? TelemetryProp.DEFAULT_ENDPOINT : endpoint;
    }

    private TelemetryPayload buildPayload(String instanceId, String eventType) {
        TelemetryPayload.TelemetryPayloadBuilder builder = TelemetryPayload.builder()
                .schema(SCHEMA_VERSION)
                .eventType(eventType)
                .instanceId(instanceId)
                .eruptVersion(EruptInformation.getEruptVersion())
                .modules(EruptCoreService.getModules())
                .eruptCount(EruptCoreService.getErupts().size())
                .javaVersion(System.getProperty("java.version"))
                .javaVendor(System.getProperty("java.vendor"))
                .os(System.getProperty("os.name"))
                .osDistro(this.osDistro())
                .arch(System.getProperty("os.arch"))
                .containerized(this.containerized())
                .springBootVersion(SpringBootVersion.getVersion())
                .locale(eruptProp.getDefaultLocales())
                .timezone(TimeZone.getDefault().getID());
        // the datasource is optional, mongo-only or memory-only deployments have none
        DataSource dataSource = dataSourceProvider.getIfUnique();
        if (null != dataSource) {
            try (Connection connection = dataSource.getConnection()) {
                DatabaseMetaData metaData = connection.getMetaData();
                builder.dbType(metaData.getDatabaseProductName());
                // major.minor only, coarse enough to stay anonymous, precise enough to drive SQL compatibility work
                builder.dbVersion(metaData.getDatabaseMajorVersion() + EruptConst.DOT + metaData.getDatabaseMinorVersion());
            } catch (Throwable ignored) {
                // a database that cannot be probed is simply reported as unknown
            }
        }
        return builder.build();
    }

    /**
     * Linux distribution id read from os-release, for example {@code kylin} / {@code uos} /
     * {@code openeuler} / {@code ubuntu}. {@code os.name} reports a bare 'Linux' for every
     * distribution, so without this the domestic OS share cannot be measured at all.
     * Returns null on Windows and macOS, where os.name is already specific enough.
     */
    private String osDistro() {
        for (String path : OS_RELEASE_FILES) {
            File file = new File(path);
            if (!file.isFile()) continue;
            try {
                String id = parseOsRelease(FileUtils.readLines(file, StandardCharsets.UTF_8));
                if (null != id) return id;
            } catch (Throwable ignored) {
                // unreadable os-release, report the distribution as unknown
            }
        }
        return null;
    }

    /**
     * Pull the {@code ID=} entry out of os-release content. The value may be quoted, and
     * {@code VERSION_ID} / {@code ID_LIKE} must not be mistaken for it.
     */
    static String parseOsRelease(List<String> lines) {
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.startsWith("ID=")) continue;
            String id = StringUtils.strip(trimmed.substring(3).trim(), "\"'");
            if (StringUtils.isBlank(id)) continue;
            return StringUtils.left(id.toLowerCase(), MAX_OS_DISTRO_CHARS);
        }
        return null;
    }

    // Container detection covering docker, podman and kubernetes, all of them cheap local checks
    private boolean containerized() {
        try {
            if (new File("/.dockerenv").exists() || new File("/run/.containerenv").exists()) return true;
            if (StringUtils.isNotBlank(System.getenv("KUBERNETES_SERVICE_HOST"))) return true;
            File cgroup = new File("/proc/1/cgroup");
            if (cgroup.isFile()) return isContainerCgroup(FileUtils.readFileToString(cgroup, StandardCharsets.UTF_8));
        } catch (Throwable ignored) {
            // not readable, assume bare metal
        }
        return false;
    }

    // Match the runtime markers cgroup v1 writes into the pid 1 hierarchy paths
    static boolean isContainerCgroup(String content) {
        if (StringUtils.isBlank(content)) return false;
        return content.contains("/docker") || content.contains("kubepods")
                || content.contains("containerd") || content.contains("/lxc");
    }

    private String readBody(HttpURLConnection conn) throws Exception {
        try (InputStream in = conn.getInputStream()) {
            byte[] buf = new byte[MAX_RESPONSE_BYTES];
            int len = 0;
            int read;
            while (len < buf.length && (read = in.read(buf, len, buf.length - len)) != -1) {
                len += read;
            }
            return new String(buf, 0, len, StandardCharsets.UTF_8);
        }
    }

    /**
     * The endpoint may answer with {@code {"level":"info|warn","message":"..."}} to surface a
     * release or security advisory. The response is remote input: it is only ever logged, and
     * it is truncated and stripped of control characters first so it cannot forge log lines.
     */
    private void handleAdvisory(String body) {
        if (StringUtils.isBlank(body)) return;
        try {
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
            if (!json.has("message")) return;
            String message = this.sanitize(json.get("message").getAsString());
            if (StringUtils.isBlank(message)) return;
            if (json.has("level") && "warn".equalsIgnoreCase(json.get("level").getAsString())) {
                log.warn("[erupt] {}", message);
            } else {
                log.info("[erupt] {}", message);
            }
        } catch (Throwable ignored) {
            // malformed advisory, nothing to act on
        }
    }

    // Drop control characters so a response cannot inject fake log lines, and cap the length
    private String sanitize(String text) {
        if (null == text) return null;
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (sb.length() >= MAX_ADVISORY_CHARS) break;
            if (c >= ' ' && c != 0x7F) sb.append(c);
        }
        return sb.toString().trim();
    }

}
