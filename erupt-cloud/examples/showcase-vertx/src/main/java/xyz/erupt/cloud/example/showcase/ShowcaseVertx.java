package xyz.erupt.cloud.example.showcase;

import io.vertx.core.Vertx;
import xyz.erupt.cloud.node.lite.Node;
import xyz.erupt.cloud.node.vertx.EruptNodeVertx;

/** The showcase on a Vert.x HTTP server of its own (erupt-cloud-node-vertx). */
public final class ShowcaseVertx {

    public static void main(String[] args) throws Exception {
        Node node = Showcase.node();
        Vertx vertx = Vertx.vertx();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            node.close();
            vertx.close();
        }));
        EruptNodeVertx.serve(vertx, node, Showcase.port()).onFailure(e -> {
            e.printStackTrace();
            System.exit(1);
        });
    }

    private ShowcaseVertx() {
    }
}
