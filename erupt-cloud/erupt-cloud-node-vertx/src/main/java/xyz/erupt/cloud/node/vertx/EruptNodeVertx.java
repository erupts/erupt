package xyz.erupt.cloud.node.vertx;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import xyz.erupt.cloud.common.consts.CloudRestApiConst;
import xyz.erupt.cloud.node.lite.Node;

import java.util.List;

/**
 * Serves an erupt node from a Vert.x Web router, next to the application's own routes.
 *
 * <pre>{@code
 * EruptNodeVertx.serve(vertx, node, 9090);              // a server of its own
 * EruptNodeVertx.mount(router, node);                   // or on the application's router,
 * server.listen(9090).onSuccess(s -> node.connect(s.actualPort()));
 * }</pre>
 *
 * @author YuePeng
 * date 2026/10/15
 */
public final class EruptNodeVertx {

    // what the erupt server calls on a node
    private static final List<String> PATHS = List.of(Node.ERUPT_API + "/*", CloudRestApiConst.NODE_HEALTH);

    private EruptNodeVertx() {
    }

    /** Routes the node's paths of {@code router} to {@code node}, on worker threads: repositories block. */
    public static Router mount(Router router, Node node) {
        Handler<RoutingContext> handler = ctx -> {
            HttpServerRequest req = ctx.request();
            Buffer body = ctx.body().buffer();
            String target = Node.Request.target(ctx.mountPoint(), req.path(), req.query());
            Node.Response resp = node.handle(new Node.Request(req.method().name(), target, req::getHeader,
                    body == null ? new byte[0] : body.getBytes()));
            ctx.response().setStatusCode(resp.status()).putHeader("Content-Type", resp.contentType()).end(Buffer.buffer(resp.body()));
        };
        for (String path : PATHS) router.route(path).handler(BodyHandler.create()).blockingHandler(handler, false);
        return router;
    }

    /** A server of the node's own on {@code port} (0 for any), connected to the erupt server once it listens. */
    public static Future<HttpServer> serve(Vertx vertx, Node node, int port) {
        return vertx.createHttpServer().requestHandler(mount(Router.router(vertx), node)).listen(port)
                .onSuccess(server -> node.connect(server.actualPort()));
    }

}
