package xyz.erupt.config.boot;

import org.apache.commons.logging.Log;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import xyz.erupt.config.model.ConfigProfile;
import xyz.erupt.config.service.EruptConfigService;

import java.util.List;
import java.util.Map;

/**
 * Puts the profiles' items into the Spring environment before any bean is created, so a
 * {@code @Value} sees them from the first injection. Runs after the config files are loaded, so
 * the active profiles are known. Reads the database with the {@code spring.datasource} settings;
 * a database without the tables yet (first boot) is skipped.
 *
 * @author YuePeng
 */
public class EruptConfigEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String QUOTED_IDENTIFIERS = "spring.jpa.properties.hibernate.globally_quoted_identifiers";

    private final Log log;

    public EruptConfigEnvironmentPostProcessor(DeferredLogFactory logFactory) {
        this.log = logFactory.getLog(EruptConfigEnvironmentPostProcessor.class);
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String url = environment.getProperty("spring.datasource.url");
        if (null == url || url.isBlank()) return;
        List<String> profiles = ConfigProfile.layers(environment);
        try {
            Map<String, Object> values = ConfigJdbcReader.read(url, environment.getProperty("spring.datasource.username"),
                    environment.getProperty("spring.datasource.password"), profiles, environment.getProperty(QUOTED_IDENTIFIERS, Boolean.class, false));
            environment.getPropertySources().addFirst(new MapPropertySource(EruptConfigService.SOURCE, Map.copyOf(values)));
            log.info("erupt-config: " + values.size() + " values of profiles " + profiles);
        } catch (Exception e) {
            // no table yet, or no database reachable: the context still starts, with the files' values
            log.warn("erupt-config: no values loaded before startup, the files' values apply until the first refresh (" + e.getMessage() + ")");
        }
    }

}
