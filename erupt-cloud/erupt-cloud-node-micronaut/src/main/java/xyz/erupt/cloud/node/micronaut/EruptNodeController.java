package xyz.erupt.cloud.node.micronaut;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import xyz.erupt.cloud.node.lite.Node;

import java.net.URI;

/**
 * Serves the application's {@link Node} bean on Micronaut's HTTP server: routes the paths the erupt server calls,
 * while {@link EruptNodeLifecycle} connects and deregisters it. Without a Node bean it does nothing.
 *
 * <pre>{@code
 * @Factory
 * public class EruptNodeConfig {
 *     @Singleton
 *     Node node(@Value("${erupt.cloud-node.access-token}") String token, DataSource dataSource, BeanContext ctx) {
 *         return new Node("blog", token, "http://erupt-server:8080")
 *                 .factory(type -> ctx.findBean(type).orElse(null))
 *                 .register(Article.class, new JdbcRepository<>(dataSource, "article"));
 *     }
 * }
 * }</pre>
 *
 * @author YuePeng
 * date 2026/10/15
 */
@Controller
@Requires(beans = Node.class)
@ExecuteOn(TaskExecutors.BLOCKING) // repositories block
public class EruptNodeController {

    private final Node node;

    private final String contextPath;

    public EruptNodeController(Node node, @Value("${micronaut.server.context-path:}") String contextPath) {
        this.node = node;
        this.contextPath = contextPath;
    }

    // the node only answers GET and POST
    @Get(uris = {Node.ERUPT_API + "/{+path}", "/node-health"}, produces = MediaType.ALL)
    HttpResponse<byte[]> get(HttpRequest<?> request) {
        return serve(request, new byte[0]);
    }

    @Post(uris = {Node.ERUPT_API + "/{+path}", "/node-health"}, consumes = MediaType.ALL, produces = MediaType.ALL)
    HttpResponse<byte[]> post(HttpRequest<?> request, @Body @Nullable byte[] body) {
        return serve(request, body == null ? new byte[0] : body);
    }

    private HttpResponse<byte[]> serve(HttpRequest<?> request, byte[] body) {
        // raw path and query: the signature covers them as sent
        URI uri = request.getUri();
        String target = Node.Request.target(contextPath, uri.getRawPath(), uri.getRawQuery());
        Node.Response resp = node.handle(new Node.Request(request.getMethodName(), target, request.getHeaders()::get, body));
        return HttpResponse.<byte[]>status(HttpStatus.valueOf(resp.status()))
                .contentType(resp.contentType()).body(resp.body());
    }

}
