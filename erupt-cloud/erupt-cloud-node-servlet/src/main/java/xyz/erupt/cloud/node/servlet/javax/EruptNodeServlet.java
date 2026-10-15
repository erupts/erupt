package xyz.erupt.cloud.node.servlet.javax;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import xyz.erupt.cloud.common.consts.CloudRestApiConst;
import xyz.erupt.cloud.node.lite.Node;

import java.io.IOException;

/**
 * Serves an erupt node from a javax.servlet 3.1+ container (Tomcat 8.5 / 9, Jetty 9.4 / 10, Undertow 2.0...), next to the
 * application's own servlets; the twin of the jakarta.servlet {@link xyz.erupt.cloud.node.servlet.EruptNodeServlet}.
 * A servlet does not know the port it is served on, so the node connects in {@link #init} only when its
 * {@link Node#address(String) address} is set; otherwise call {@code node.connect(port)} once the container listens.
 *
 * <pre>{@code
 * public class EruptNodeInitializer implements ServletContainerInitializer {   // or a @WebListener's contextInitialized
 *     public void onStartup(Set<Class<?>> classes, ServletContext context) {
 *         EruptNodeServlet.mount(context, new Node("blog", token, "http://erupt-server:8080")
 *                 .address("http://10.0.0.12:8080/app")
 *                 .register(Article.class, new JdbcRepository<>(dataSource, "article")));
 *     }
 * }
 * }</pre>
 *
 * @author YuePeng
 * date 2026/10/15
 */
public class EruptNodeServlet extends HttpServlet {

    // what the erupt server calls on a node
    public static final String[] MAPPINGS = {Node.ERUPT_API + "/*", CloudRestApiConst.NODE_HEALTH};

    private final transient Node node;

    public EruptNodeServlet(Node node) {
        this.node = node;
    }

    /** Adds the node's servlet to {@code context} on the node's paths; from a container initializer or context listener. */
    public static ServletRegistration.Dynamic mount(ServletContext context, Node node) {
        ServletRegistration.Dynamic registration = context.addServlet("eruptNode", new EruptNodeServlet(node));
        registration.addMapping(MAPPINGS);
        registration.setLoadOnStartup(1);
        return registration;
    }

    // a container may still be opening its connectors: the heartbeat retries until the server's probe passes
    @Override
    public void init() {
        if (node.address() != null) node.connect();
    }

    @Override
    public void destroy() {
        node.close();
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // the request URI and query string are raw, as the signature covers them
        String target = Node.Request.target(req.getContextPath(), req.getRequestURI(), req.getQueryString());
        Node.Response r = node.handle(new Node.Request(req.getMethod(), target, req::getHeader, req.getInputStream().readAllBytes()));
        resp.setStatus(r.status());
        resp.setContentType(r.contentType());
        resp.setContentLength(r.body().length);
        resp.getOutputStream().write(r.body());
    }

}
