package xyz.erupt.config.service;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.stereotype.Service;
import xyz.erupt.config.boot.ConfigValues;
import xyz.erupt.config.event.EruptConfigRefreshedEvent;
import xyz.erupt.config.model.ConfigItem;
import xyz.erupt.config.model.ConfigProfile;
import xyz.erupt.config.prop.EruptConfigProperties;
import xyz.erupt.jpa.dao.EruptDao;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The running values: the items of the layered profiles ({@link #layers()}), merged and held as
 * the property source named {@value #SOURCE} in front of every file. {@link #refresh()} reloads
 * them, sets the {@code @Value} fields again and publishes {@link EruptConfigRefreshedEvent}; a
 * poller does the same when another instance changed something.
 *
 * @author YuePeng
 */
@Slf4j
@Service
public class EruptConfigService {

    public static final String SOURCE = "erupt-config";

    private final EruptDao eruptDao;

    private final ConfigurableEnvironment environment;

    private final EruptConfigProperties properties;

    private final ConfigValueRefresher refresher;

    private final ApplicationEventPublisher publisher;

    private ScheduledExecutorService poller;

    // what the database looked like at the last refresh: a cheap way to notice another instance's change
    private String signature = "";

    /** Where a running property came from: the item (by key) and profile that set it, its type and secrecy. */
    public record Origin(String item, String profile, String type, boolean secret) {
    }

    private Map<String, Origin> origins = Map.of();

    public EruptConfigService(EruptDao eruptDao, ConfigurableEnvironment environment, EruptConfigProperties properties,
                              ConfigValueRefresher refresher, ApplicationEventPublisher publisher) {
        this.eruptDao = eruptDao;
        this.environment = environment;
        this.properties = properties;
        this.refresher = refresher;
        this.publisher = publisher;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        this.ensureProfiles();
        this.refresh();
        if (properties.getRefreshSeconds() > 0) {
            poller = Executors.newSingleThreadScheduledExecutor(runnable -> {
                Thread thread = new Thread(runnable, "erupt-config");
                thread.setDaemon(true);
                return thread;
            });
            poller.scheduleWithFixedDelay(this::refreshIfChanged, properties.getRefreshSeconds(), properties.getRefreshSeconds(), TimeUnit.SECONDS);
        }
    }

    @PreDestroy
    public void stop() {
        if (null != poller) poller.shutdownNow();
    }

    // The profiles this instance reads exist as rows, so the page has them on a fresh database
    private void ensureProfiles() {
        List<String> present = eruptDao.lambdaQuery(ConfigProfile.class).in(ConfigProfile::getCode, this.layers()).list().stream().map(ConfigProfile::getCode).toList();
        for (String code : this.layers()) {
            if (present.contains(code)) continue;
            ConfigProfile profile = new ConfigProfile();
            profile.setCode(code);
            eruptDao.persistAndFlush(profile);
        }
    }

    /** The profiles this instance reads, in override order. */
    public List<String> layers() {
        return ConfigProfile.layers(environment);
    }

    /** Whether a change to this profile's items changes what this instance reads. */
    public boolean reads(String profile) {
        return this.layers().contains(profile);
    }

    // Enabled items only. One query per layer keeps the override order without reading the profile
    // back from an item just added, whose reference may be a bare id inside the same transaction
    private List<ConfigItem> items() {
        List<ConfigItem> items = new ArrayList<>();
        for (String layer : this.layers()) {
            items.addAll(eruptDao.lambdaQuery(ConfigItem.class).eq(ConfigItem::getEnabled, true)
                    .with(ConfigItem::getProfile).eq(ConfigProfile::getCode, layer).with().list());
        }
        return items;
    }

    /** The running values with their origin, in key order: what this instance reads right now. */
    public Map<String, Origin> origins() {
        return origins;
    }

    /** Reloads the running values from the database and tells the beans. */
    public synchronized Set<String> refresh() {
        List<ConfigItem> items = this.items();
        Map<String, Object> next = new LinkedHashMap<>();
        Map<String, Origin> from = new TreeMap<>();
        for (ConfigItem item : items) {
            // flattened apart, so a KEY_VALUE item's entries each carry the item's origin
            Map<String, Object> flat = new LinkedHashMap<>();
            ConfigValues.flatten(item.getKey(), item.getType(), item.getValue(), flat);
            for (String key : flat.keySet()) from.put(key, new Origin(item.getKey(), item.getProfile().getCode(), item.getType(), Boolean.TRUE.equals(item.getSecret())));
            next.putAll(flat);
        }
        MutablePropertySources sources = environment.getPropertySources();
        Map<String, Object> previous = sources.contains(SOURCE) ? ((MapPropertySource) sources.get(SOURCE)).getSource() : Map.of();
        Set<String> changed = new TreeSet<>();
        for (String key : next.keySet()) if (!Objects.equals(previous.get(key), next.get(key))) changed.add(key);
        for (String key : previous.keySet()) if (!next.containsKey(key)) changed.add(key);
        MapPropertySource source = new MapPropertySource(SOURCE, Collections.unmodifiableMap(next));
        if (sources.contains(SOURCE)) sources.replace(SOURCE, source);
        else sources.addFirst(source);
        this.signature = signature(items);
        this.origins = Collections.unmodifiableMap(from);
        if (!changed.isEmpty()) {
            int fields = refresher.refresh(environment);
            log.info("erupt-config: {} keys changed, {} @Value fields updated", changed.size(), fields);
            publisher.publishEvent(new EruptConfigRefreshedEvent(this, changed));
        }
        return changed;
    }

    private void refreshIfChanged() {
        try {
            if (!signature(this.items()).equals(signature)) this.refresh();
        } catch (RuntimeException e) {
            log.warn("erupt-config: refresh failed: {}", e.getMessage());
        }
    }

    private static String signature(List<ConfigItem> items) {
        LocalDateTime latest = items.stream().map(ConfigItem::getUpdateTime).filter(Objects::nonNull).max(Comparator.naturalOrder()).orElse(null);
        return items.size() + "@" + latest;
    }

}
