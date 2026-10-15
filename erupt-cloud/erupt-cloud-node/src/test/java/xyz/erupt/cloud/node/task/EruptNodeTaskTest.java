package xyz.erupt.cloud.node.task;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.annotation.Import;
import xyz.erupt.cloud.common.consts.CloudRestApiConst;
import xyz.erupt.cloud.node.config.EruptNodeProp;

import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EruptNodeTaskTest {

    final BlockingQueue<String> registered = new LinkedBlockingQueue<>(), removed = new LinkedBlockingQueue<>();
    HttpServer eruptServer;

    @SpringBootConfiguration
    @ImportAutoConfiguration(ServletWebServerFactoryAutoConfiguration.class)
    @Import({EruptNodeTask.class, EruptNodeProp.class})
    static class App {
    }

    @AfterEach
    void tearDown() {
        eruptServer.stop(0);
    }

    @Test
    void registersTheListeningPortAndDeregisters() throws Exception {
        eruptServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        eruptServer.createContext(CloudRestApiConst.REGISTER_NODE, ex -> {
            registered.add(new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            ex.sendResponseHeaders(200, -1);
            ex.close();
        });
        eruptServer.createContext(CloudRestApiConst.REMOVE_INSTANCE_NODE, ex -> {
            removed.add(URLDecoder.decode(new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8), StandardCharsets.UTF_8));
            ex.sendResponseHeaders(200, -1);
            ex.close();
        });
        eruptServer.start();
        // server.port 0: the address must carry the port actually bound, and the context path
        ServletWebServerApplicationContext context = (ServletWebServerApplicationContext) new SpringApplicationBuilder(App.class).properties(
                "server.port=0", "server.servlet.context-path=/app",
                "erupt.cloud-node.node-name=spring-node", "erupt.cloud-node.access-token=secret",
                "erupt.cloud-node.server-addresses=http://127.0.0.1:" + eruptServer.getAddress().getPort(),
                "erupt.cloud-node.drain-timeout=0").run();
        String address = ":" + context.getWebServer().getPort() + "/app";
        String payload = registered.poll(10, TimeUnit.SECONDS);
        assertNotNull(payload);
        assertTrue(payload.contains("\"nodeName\":\"spring-node\"") && payload.contains(address + "\"]"), payload);
        context.close();
        String form = removed.poll(10, TimeUnit.SECONDS);
        assertNotNull(form);
        assertTrue(form.startsWith("nodeName=spring-node&accessToken=secret&locations=http://") && form.endsWith(address), form);
    }
}
