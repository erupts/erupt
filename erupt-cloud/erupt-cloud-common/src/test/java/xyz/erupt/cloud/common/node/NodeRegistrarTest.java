package xyz.erupt.cloud.common.node;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import xyz.erupt.cloud.common.consts.CloudRestApiConst;
import xyz.erupt.cloud.common.model.NodeInfo;

import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NodeRegistrarTest {

    final BlockingQueue<String> registered = new LinkedBlockingQueue<>(), removed = new LinkedBlockingQueue<>();
    HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) server.stop(0);
    }

    String start() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext(CloudRestApiConst.REGISTER_NODE, ex -> {
            registered.add(new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            ex.sendResponseHeaders(200, -1);
            ex.close();
        });
        server.createContext(CloudRestApiConst.REMOVE_INSTANCE_NODE, ex -> {
            removed.add(URLDecoder.decode(new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8), StandardCharsets.UTF_8));
            ex.sendResponseHeaders(200, -1);
            ex.close();
        });
        server.start();
        return "http://127.0.0.1:" + server.getAddress().getPort() + "/";
    }

    @Test
    void registersHeartbeatsAndDeregisters() throws Exception {
        NodeRegistrar registrar = new NodeRegistrar("blog", "secret", List.of(start()));
        int[] beats = {0};
        registrar.start(Duration.ofMillis(50), () -> {
            NodeInfo info = new NodeInfo();
            info.setVersion("2.3.0");
            info.setNodeAddress(new String[]{"http://10.0.0.1:8080/app", "http://10.0.0.2:8080/app"});
            info.setErupts(List.of("Article"));
            info.setResources(Map.of("cube", List.of("Sales")));
            info.setInstanceId("overwritten");
            beats[0]++;
            return info;
        });
        String first = registered.poll(5, TimeUnit.SECONDS);
        assertNotNull(first);
        assertTrue(first.contains("\"nodeName\":\"blog\"") && first.contains("\"accessToken\":\"secret\""), first);
        assertTrue(first.contains("\"nodeAddress\":[\"http://10.0.0.1:8080/app\",\"http://10.0.0.2:8080/app\"]"), first);
        assertTrue(first.contains("\"erupts\":[\"Article\"]") && first.contains("\"resources\":{\"cube\":[\"Sales\"]}"), first);
        assertTrue(first.contains("\"instanceId\":\"" + registrar.instanceId() + "\""), first);
        assertNotNull(registered.poll(5, TimeUnit.SECONDS), "heart-beats");
        assertTrue(beats[0] >= 2, "info asked for every beat");
        registrar.close();
        assertEquals("nodeName=blog&accessToken=secret&locations=http://10.0.0.1:8080/app,http://10.0.0.2:8080/app",
                removed.poll(5, TimeUnit.SECONDS));
    }

    @Test
    void escapesStrings() {
        NodeInfo info = new NodeInfo();
        info.setNodeName("a\"b\\c\n\u0001");
        assertTrue(NodeRegistrar.json(info).contains("\"nodeName\":\"a\\\"b\\\\c\\n\\u0001\""), NodeRegistrar.json(info));
    }

    @Test
    void localAddress() {
        NodeRegistrar registrar = new NodeRegistrar("blog", "secret", List.of("http://127.0.0.1:9999"));
        assertEquals("http://127.0.0.1:8080/app", registrar.localAddress("http", 8080, "/app/"));
        assertEquals("https://127.0.0.1:443", registrar.localAddress("https", 443, null));
    }
}
