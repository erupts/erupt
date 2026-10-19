package xyz.erupt.config.event;

import org.springframework.context.ApplicationEvent;

import java.util.Set;

/**
 * Published after the running values changed; {@link #getKeys()} are the keys that differ.
 *
 * @author YuePeng
 */
public class EruptConfigRefreshedEvent extends ApplicationEvent {

    private final Set<String> keys;

    public EruptConfigRefreshedEvent(Object source, Set<String> keys) {
        super(source);
        this.keys = keys;
    }

    public Set<String> getKeys() {
        return keys;
    }

}
