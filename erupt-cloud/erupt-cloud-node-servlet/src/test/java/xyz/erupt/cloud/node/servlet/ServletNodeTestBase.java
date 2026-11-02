package xyz.erupt.cloud.node.servlet;

import com.sun.net.httpserver.HttpServer;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** One container serving a node under the context path {@link #CONTEXT}, beside an application servlet on /hello. */
public abstract class ServletNodeTestBase {

    protected static final String TOKEN = "node-secret", CONTEXT = "/app";

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

    /** Starts the container with the node mounted by the servlet's {@code mount}; returns its port. */
    protected abstract int start(Node node) throws Exception;

    protected abstract void stop() throws Exception;

    @AfterEach
    void tearDown() throws Exception {
        stop();
        eruptServer.stop(0);
    }

    @Test
    void connectsOnThePortGiven() throws Exception {
        Node node = node();
        int port = start(node);
        node.connect(port);
        assertTrue(registered.get(10, TimeUnit.SECONDS).contains(":" + port + "\""), "registers the port it listens on");
        assertServes(port);
    }

    @Test
    void connectsOnInitWithAnAddress() throws Exception {
        Node node = node().address("http://127.0.0.1:1" + CONTEXT);
        int port = start(node);
        assertTrue(registered.get(10, TimeUnit.SECONDS).contains(":1" + CONTEXT + "\""), "registers the address set");
        assertServes(port);
    }

    private Node node() throws Exception {
        eruptServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        eruptServer.createContext("/erupt-cloud-api/register-node", ex -> {
            registered.complete(new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            ex.sendResponseHeaders(200, -1);
            ex.close();
        });
        eruptServer.start();
        return new Node("servlet-node", TOKEN, "http://127.0.0.1:" + eruptServer.getAddress().getPort())
                .register(Item.class, new MemoryRepository<>());
    }

    private void assertServes(int port) throws Exception {
        assertEquals("app", get(port, CONTEXT + "/hello").body());
        assertEquals("servlet-node", get(port, CONTEXT + "/node-health").body());
        assertEquals(404, get(port, "/node-health").statusCode(), "only under the context path");
        assertTrue(signedGet(port, "/erupt-api/build/Item?_=a%2Fb").contains("\"Item\""));
    }

    HttpResponse<String> get(int port, String path) throws Exception {
        return http.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path)).build(), HttpResponse.BodyHandlers.ofString());
    }

    // signed over the target below the context path, as the server signs it
    String signedGet(int port, String target) throws Exception {
        long now = System.currentTimeMillis() / 1000;
        HttpRequest req = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + CONTEXT + target))
                .header(CloudSignature.HEADER_TIMESTAMP, String.valueOf(now))
                .header(CloudSignature.HEADER_SIGNATURE, CloudSignature.sign(TOKEN, "GET", target, now, null, null, new byte[0]))
                .header("erupt", "Item").build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode(), resp.body());
        return resp.body();
    }
}
