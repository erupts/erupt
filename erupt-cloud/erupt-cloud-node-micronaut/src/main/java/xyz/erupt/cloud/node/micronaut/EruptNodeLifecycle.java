package xyz.erupt.cloud.node.micronaut;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.runtime.server.event.ServerShutdownEvent;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import jakarta.inject.Singleton;
import xyz.erupt.cloud.node.lite.Node;

/**
 * Connects the application's {@link Node} once Micronaut's HTTP server listens (the erupt server probes the node
 * while registering it) and deregisters it on shutdown.
 *
 * @author YuePeng
 * date 2026/10/15
 */
@Singleton
@Requires(beans = Node.class)
public class EruptNodeLifecycle {

    private final Node node;

    private final String contextPath;

    public EruptNodeLifecycle(Node node, @Value("${micronaut.server.context-path:}") String contextPath) {
        this.node = node;
        this.contextPath = contextPath;
    }

    @EventListener
    void started(ServerStartupEvent event) {
        node.connect(event.getSource().getPort(), contextPath.isEmpty() ? null : contextPath);
    }

    @EventListener
    void stopped(ServerShutdownEvent event) {
        node.close();
    }

}
