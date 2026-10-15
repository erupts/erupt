package xyz.erupt.cloud.node.micronaut;

import com.sun.net.httpserver.HttpServer;
import io.micronaut.context.ApplicationContext;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.runtime.server.EmbeddedServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.cloud.common.http.CloudSignature;
import xyz.erupt.cloud.node.lite.MemoryRepository;
import xyz.erupt.cloud.node.lite.Node;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EruptNodeControllerTest {

    static final String TOKEN = "node-secret", CONTEXT = "/app";

    final CompletableFuture<String> registered = new CompletableFuture<>();
    final HttpClient http = HttpClient.newHttpClient();
    HttpServer eruptServer;
    ApplicationContext context;

    @Erupt(name = "Item")
    public static class Item {
        @EruptField
        Long id;
        @EruptField(views = @View(title = "Name"), edit = @Edit(title = "Name"))
        String name;
    }

    @Controller("/hello")
    static class Hello {
        @Get
        String hello() {
            return "app";
        }
    }

    @AfterEach
    void tearDown() {
        if (context != null) context.close();
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
        Node node = new Node("micronaut-node", TOKEN, "http://127.0.0.1:" + eruptServer.getAddress().getPort())
                .register(Item.class, new MemoryRepository<>());
        context = ApplicationContext.builder().singletons(node)
                .properties(Map.of("micronaut.server.port", -1, "micronaut.server.context-path", CONTEXT)).start();
        int port = context.getBean(EmbeddedServer.class).start().getPort();
        assertTrue(registered.get(10, TimeUnit.SECONDS).contains(":" + port + CONTEXT + "\""), "registers the port and context path");
        assertEquals("app", get(port, CONTEXT + "/hello"));
        assertEquals("micronaut-node", get(port, CONTEXT + "/node-health"));
        assertTrue(signedGet(port, "/erupt-api/build/Item?_=a%2Fb").contains("\"Item\""));
        assertTrue(signedPost(port, "/erupt-api/data/table/Item", "{\"pageIndex\":1,\"pageSize\":10}").contains("\"total\""));
    }

    String get(int port, String path) throws Exception {
        return http.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path)).build(), HttpResponse.BodyHandlers.ofString()).body();
    }

    String signedGet(int port, String target) throws Exception {
        return signed(port, target, "GET", new byte[0]);
    }

    String signedPost(int port, String target, String json) throws Exception {
        return signed(port, target, "POST", json.getBytes(StandardCharsets.UTF_8));
    }

    // signed over the target below the context path, as the server signs it, with the user's power enforced there
    String signed(int port, String target, String method, byte[] body) throws Exception {
        long now = System.currentTimeMillis() / 1000;
        String user = Base64.getEncoder().encodeToString("{\"uid\":1,\"account\":\"admin\"}".getBytes(StandardCharsets.UTF_8));
        HttpRequest req = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + CONTEXT + target))
                .header(CloudSignature.HEADER_TIMESTAMP, String.valueOf(now))
                .header(CloudSignature.HEADER_SIGNATURE, CloudSignature.sign(TOKEN, method, target, now, "power", user, body))
                .header(CloudSignature.HEADER_ENFORCED, "power").header("user", user)
                .header("Content-Type", "application/json")
                .header("erupt", "Item").method(method, HttpRequest.BodyPublishers.ofByteArray(body)).build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode(), resp.body());
        return resp.body();
    }
}
