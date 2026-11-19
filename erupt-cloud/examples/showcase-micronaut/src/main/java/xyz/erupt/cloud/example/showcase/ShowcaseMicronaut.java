package xyz.erupt.cloud.example.showcase;

import io.micronaut.context.ApplicationContext;
import io.micronaut.runtime.server.EmbeddedServer;
import xyz.erupt.cloud.node.lite.Node;

import java.util.Map;

/** The showcase on Micronaut: the node is a singleton, which erupt-cloud-node-micronaut serves and connects. */
public final class ShowcaseMicronaut {

    public static void main(String[] args) throws Exception {
        Node node = Showcase.node();
        ApplicationContext context = ApplicationContext.builder().singletons(node)
                .properties(Map.of("micronaut.server.port", Showcase.port())).start();
        node.factory(type -> context.findBean(type).orElse(null));
        context.getBean(EmbeddedServer.class).start();
        Runtime.getRuntime().addShutdownHook(new Thread(context::close));
    }

    private ShowcaseMicronaut() {
    }
}
