package xyz.erupt.cloud.example.showcase;

import io.quarkus.arc.Arc;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import xyz.erupt.cloud.node.lite.Node;

/**
 * The showcase on Quarkus: the node is a {@code @Singleton} ({@code Node} is final, so it cannot be a proxied bean),
 * which erupt-cloud-node-quarkus serves on Quarkus' HTTP server and connects. The port is {@code PORT}
 * (application.properties).
 */
public class ShowcaseQuarkus {

    @Produces
    @Singleton
    Node node() throws Exception {
        return Showcase.node().factory(type -> Arc.container().instance(type).get());
    }
}
