package xyz.erupt.cloud.example.showcase;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import xyz.erupt.cloud.node.lite.Node;
import xyz.erupt.cloud.node.servlet.EruptNodeServlet;

import java.nio.file.Files;

/** The showcase in a servlet container (erupt-cloud-node-servlet), here an embedded Tomcat, at the root context. */
public final class ShowcaseServlet {

    public static void main(String[] args) throws Exception {
        Node node = Showcase.node();
        int port = Showcase.port();
        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir(Files.createTempDirectory("tomcat").toString());
        tomcat.setPort(port);
        Context context = tomcat.addContext("", null);
        // the servlet does not know its port: connect once Tomcat listens
        context.addServletContainerInitializer((classes, servletContext) -> EruptNodeServlet.mount(servletContext, node), null);
        tomcat.getConnector();
        tomcat.start();
        node.connect(tomcat.getConnector().getLocalPort());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                tomcat.stop();
            } catch (Exception ignored) {
                // stopping anyway
            }
        }));
        tomcat.getServer().await();
    }

    private ShowcaseServlet() {
    }
}
