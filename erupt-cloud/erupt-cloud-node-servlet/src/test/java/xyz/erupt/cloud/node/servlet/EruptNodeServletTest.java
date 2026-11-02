package xyz.erupt.cloud.node.servlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import xyz.erupt.cloud.node.lite.Node;

import java.io.IOException;
import java.nio.file.Files;

/** jakarta.servlet on Tomcat 10.1. */
class EruptNodeServletTest extends ServletNodeTestBase {

    Tomcat tomcat;

    @Override
    protected int start(Node node) throws Exception {
        tomcat = new Tomcat();
        tomcat.setBaseDir(Files.createTempDirectory("tomcat").toString());
        tomcat.setPort(0);
        Context context = tomcat.addContext(CONTEXT, null);
        Tomcat.addServlet(context, "hello", new HttpServlet() {
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
                resp.getWriter().write("app");
            }
        });
        context.addServletMappingDecoded("/hello", "hello");
        context.addServletContainerInitializer((classes, servletContext) -> EruptNodeServlet.mount(servletContext, node), null);
        tomcat.getConnector();
        tomcat.start();
        return tomcat.getConnector().getLocalPort();
    }

    @Override
    protected void stop() throws Exception {
        if (tomcat != null) {
            tomcat.stop();
            tomcat.destroy();
        }
    }
}
