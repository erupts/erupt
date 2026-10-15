package xyz.erupt.cloud.node.servlet.javax;

import org.eclipse.jetty.ee8.servlet.ServletContextHandler;
import org.eclipse.jetty.ee8.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import xyz.erupt.cloud.node.lite.Node;
import xyz.erupt.cloud.node.servlet.ServletNodeTestBase;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** javax.servlet on Jetty 12's ee8 environment. */
class EruptNodeServletTest extends ServletNodeTestBase {

    Server jetty;

    @Override
    protected int start(Node node) throws Exception {
        jetty = new Server(0);
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath(CONTEXT);
        context.addServlet(new ServletHolder(new HttpServlet() {
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
                resp.getWriter().write("app");
            }
        }), "/hello");
        context.addServletContainerInitializer((classes, servletContext) -> EruptNodeServlet.mount(servletContext, node));
        jetty.setHandler(context);
        jetty.start();
        return ((ServerConnector) jetty.getConnectors()[0]).getLocalPort();
    }

    @Override
    protected void stop() throws Exception {
        if (jetty != null) jetty.stop();
    }
}
