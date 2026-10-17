package xyz.erupt.cloud.node.quarkus;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.vertx.http.HttpServerStart;
import io.vertx.ext.web.Router;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.ObservesAsync;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import xyz.erupt.cloud.node.lite.Node;
import xyz.erupt.cloud.node.vertx.EruptNodeVertx;

/**
 * Serves the application's {@link Node} bean on Quarkus' HTTP server: routes the paths the erupt server calls,
 * connects once the server listens, under {@code quarkus.http.root-path}, and deregisters on shutdown. Produce the node as a {@code @Singleton}
 * ({@code Node} is final, so it cannot be proxied):
 *
 * <pre>{@code
 * @Produces @Singleton
 * Node node(@ConfigProperty(name = "erupt.cloud-node.access-token") String token, AgroalDataSource dataSource) {
 *     return new Node("blog", token, "http://erupt-server:8080")
 *             .factory(type -> Arc.container().instance(type).get())
 *             .register(Article.class, new JdbcRepository<>(dataSource, "article"));
 * }
 * }</pre>
 *
 * @author YuePeng
 * date 2026/10/15
 */
@ApplicationScoped
public class EruptNodeRoutes {

    void route(@Observes Router router, Node node) {
        EruptNodeVertx.mount(router, node);
    }

    // the application router, so the node's routes, are mounted below it
    @ConfigProperty(name = "quarkus.http.root-path", defaultValue = "/")
    String rootPath;

    // fired asynchronously, once per HTTP verticle; connect() starts only once
    void connect(@ObservesAsync HttpServerStart start, Node node) {
        node.connect(start.options().getPort(), rootPath);
    }

    void close(@Observes ShutdownEvent event, Node node) {
        node.close();
    }

}
