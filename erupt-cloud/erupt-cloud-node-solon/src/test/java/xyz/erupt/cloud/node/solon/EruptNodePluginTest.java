package xyz.erupt.cloud.node.solon;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.noear.solon.Solon;
import org.noear.solon.core.handle.MethodType;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.cloud.common.http.CloudSignature;
import xyz.erupt.cloud.node.lite.MemoryRepository;
import xyz.erupt.cloud.node.lite.Node;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EruptNodePluginTest {

    static final String TOKEN = "node-secret";

    final CompletableFuture<String> registered = new CompletableFuture<>();
    final HttpClient http = HttpClient.newHttpClient();
    HttpServer eruptServer;

    @Erupt(name = "Item")
    public static class Item {
        @EruptField
        Long id;
        @EruptField(views = @View(title = "Name"), edit = @Edit(title = "Name"))
        String name;
    }

    @AfterEach
    void tearDown() {
        Solon.stopBlock(false, 0);
        eruptServer.stop(0);
    }

    @Test
    void servesTheNodeBean() throws Exception {
        eruptServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        eruptServer.createContext("/erupt-cloud-api/register-node", ex -> {
            registered.complete(new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            ex.sendResponseHeaders(200, -1);
            ex.close();
        });
        eruptServer.start();
        Node node = new Node("solon-node", TOKEN, "http://127.0.0.1:" + eruptServer.getAddress().getPort())
                .factory(type -> Solon.context().getBean(type))
                .register(Item.class, new MemoryRepository<>());
        int port;
        try (ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        }
        Solon.start(EruptNodePluginTest.class, new String[]{"--server.port=" + port}, app -> {
            app.context().wrapAndPut(Node.class, node);
            app.router().add("/hello", MethodType.GET, ctx -> ctx.output("app"));
        });
        assertTrue(registered.get(10, TimeUnit.SECONDS).contains(":" + port + "\""), "registers the port it listens on");
        assertEquals("app", get(port, "/hello"));
        assertEquals("solon-node", get(port, "/node-health"));
        assertTrue(signedGet(port, "/erupt-api/build/Item?_=a%2Fb").contains("\"Item\""));
    }

    String get(int port, String path) throws Exception {
        return http.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path)).build(), HttpResponse.BodyHandlers.ofString()).body();
    }

    String signedGet(int port, String target) throws Exception {
        long now = System.currentTimeMillis() / 1000;
        HttpRequest req = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + target))
                .header(CloudSignature.HEADER_TIMESTAMP, String.valueOf(now))
                .header(CloudSignature.HEADER_SIGNATURE, CloudSignature.sign(TOKEN, "GET", target, now, null, null, new byte[0]))
                .header("erupt", "Item").build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode(), resp.body());
        return resp.body();
    }
}
