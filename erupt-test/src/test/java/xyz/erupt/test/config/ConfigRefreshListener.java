package xyz.erupt.test.config;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import xyz.erupt.config.event.EruptConfigRefreshedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Records every refresh event, for the tests to read.
 */
@Component
public class ConfigRefreshListener {

    final List<Set<String>> changes = new ArrayList<>();

    @EventListener
    void on(EruptConfigRefreshedEvent event) {
        changes.add(event.getKeys());
    }

}
