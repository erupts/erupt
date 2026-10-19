package xyz.erupt.config.service;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * Reads the running values by key, typed. A key is a string or an enum constant whose name is
 * the key; {@code @Value("${key}")} works too and follows changes.
 * <pre>
 * enum Keys { MAIL_HOST }   // or the dotted key as the enum's toString
 *
 * config.get("app.mail.host")
 * config.get(Keys.MAIL_HOST, Integer.class, 25)
 * </pre>
 *
 * @author YuePeng
 */
@Service
public class EruptConfig {

    private final Environment environment;

    public EruptConfig(Environment environment) {
        this.environment = environment;
    }

    public String get(String key) {
        return environment.getProperty(key);
    }

    public String get(String key, String defaultValue) {
        return environment.getProperty(key, defaultValue);
    }

    public <T> T get(String key, Class<T> type) {
        return environment.getProperty(key, type);
    }

    public <T> T get(String key, Class<T> type, T defaultValue) {
        return environment.getProperty(key, type, defaultValue);
    }

    public String get(Enum<?> key) {
        return this.get(key.toString());
    }

    public <T> T get(Enum<?> key, Class<T> type) {
        return this.get(key.toString(), type);
    }

    public <T> T get(Enum<?> key, Class<T> type, T defaultValue) {
        return this.get(key.toString(), type, defaultValue);
    }

    public boolean has(String key) {
        return environment.containsProperty(key);
    }

}
