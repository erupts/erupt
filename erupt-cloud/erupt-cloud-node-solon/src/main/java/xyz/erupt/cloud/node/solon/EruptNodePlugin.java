package xyz.erupt.cloud.node.solon;

import com.sun.net.httpserver.HttpExchange;
import org.noear.solon.Solon;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.Lifecycle;
import org.noear.solon.core.Plugin;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Handler;
import org.noear.solon.core.handle.MethodType;
import xyz.erupt.cloud.common.consts.CloudRestApiConst;
import xyz.erupt.cloud.node.lite.Node;

import java.util.List;

/**
 * Serves the application's {@link Node} bean on Solon's own HTTP server: routes the paths the erupt server calls,
 * connects once the application has started and deregisters on stop. Without a Node bean it does nothing.
 *
 * <pre>{@code
 * @Bean
 * public Node node(@Inject("${erupt.cloud-node.access-token}") String token, DataSource dataSource) {
 *     return new Node("blog", token, "http://erupt-server:8080")
 *             .factory(Solon.context()::getBean)
 *             .register(Article.class, new JdbcRepository<>(dataSource, "article"));
 * }
 * }</pre>
 *
 * @author YuePeng
 * date 2026/10/15
 */
public class EruptNodePlugin implements Plugin {

    // what the erupt server calls on a node
    private static final List<String> PATHS = List.of(Node.ERUPT_API + "/**", CloudRestApiConst.NODE_HEALTH);

    private volatile Node node;

    @Override
    public void start(AppContext context) {
        context.getBeanAsync(Node.class, bean -> {
            node = bean;
            Handler handler = ctx -> serve(bean, ctx);
            for (String path : PATHS) Solon.app().router().add(path, MethodType.ALL, handler);
        });
        context.lifecycle(new Lifecycle() {
            @Override
            public void start() {
            }

            // after the HTTP server listens: the erupt server probes the node while registering it
            @Override
            public void postStart() {
                if (node != null) node.connect(Solon.cfg().serverPort());
            }
        });
    }

    @Override
    public void stop() {
        if (node != null) node.close();
    }

    private static void serve(Node node, Context ctx) throws Exception {
        // the signature covers the query as sent; solon-server-jdkhttp alone hands it decoded
        String query = ctx.request() instanceof HttpExchange exchange ? exchange.getRequestURI().getRawQuery() : ctx.queryString();
        String target = Node.Request.target(Solon.cfg().serverContextPath(), ctx.uri().getRawPath(), query);
        Node.Response resp = node.handle(new Node.Request(ctx.method(), target, ctx::header, ctx.bodyAsBytes()));
        ctx.status(resp.status());
        ctx.contentType(resp.contentType());
        ctx.output(resp.body());
    }

}
