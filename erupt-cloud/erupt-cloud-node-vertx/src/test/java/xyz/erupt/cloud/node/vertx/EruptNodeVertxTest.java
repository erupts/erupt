package xyz.erupt.cloud.node.vertx;

import com.sun.net.httpserver.HttpServer;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

class EruptNodeVertxTest {

    static final String TOKEN = "node-secret";

    final CompletableFuture<String> registered = new CompletableFuture<>();
    final HttpClient http = HttpClient.newHttpClient();
    HttpServer eruptServer;
    Vertx vertx;
    Node node;

    @Erupt(name = "Item")
    public static class Item {
        @EruptField
        Long id;
        @EruptField(views = @View(title = "Name"), edit = @Edit(title = "Name"))
        String name;
    }

    @BeforeEach
    void setUp() throws Exception {
        eruptServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        eruptServer.createContext("/erupt-cloud-api/register-node", ex -> {
            registered.complete(new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            ex.sendResponseHeaders(200, -1);
            ex.close();
        });
        eruptServer.start();
        vertx = Vertx.vertx();
        node = new Node("vertx-node", TOKEN, "http://127.0.0.1:" + eruptServer.getAddress().getPort())
                .register(Item.class, new MemoryRepository<>());
    }

    @AfterEach
    void tearDown() {
        node.close();
        vertx.close();
        eruptServer.stop(0);
    }

    @Test
    void serve() throws Exception {
        int port = EruptNodeVertx.serve(vertx, node, 0).toCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS).actualPort();
        assertTrue(registered.get(10, TimeUnit.SECONDS).contains(":" + port + "\""), "registers the port it listens on");
        assertEquals("vertx-node", get(port, "/node-health"));
        assertTrue(signedGet(port, "", "/erupt-api/build/Item?_=a%2Fb").contains("\"Item\""));
    }

    @Test
    void mountBesideApplicationRoutes() throws Exception {
        Router app = Router.router(vertx);
        app.get("/hello").handler(ctx -> ctx.end("app"));
        Router nodeRoutes = EruptNodeVertx.mount(Router.router(vertx), node);
        app.route("/admin/*").subRouter(nodeRoutes);
        int port = vertx.createHttpServer().requestHandler(app).listen(0)
                .toCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS).actualPort();
        node.connect(port, "/admin");
        assertTrue(registered.get(10, TimeUnit.SECONDS).contains(":" + port + "/admin\""), "registers the mount point");
        assertEquals("app", get(port, "/hello"));
        assertEquals("vertx-node", get(port, "/admin/node-health"));
        // the server signs the path below the node's address, not the mount point
        assertTrue(signedGet(port, "/admin", "/erupt-api/build/Item").contains("\"Item\""));
    }

    String get(int port, String path) throws Exception {
        return http.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path)).build(), HttpResponse.BodyHandlers.ofString()).body();
    }

    String signedGet(int port, String mount, String target) throws Exception {
        long now = System.currentTimeMillis() / 1000;
        HttpRequest req = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + mount + target))
                .header(CloudSignature.HEADER_TIMESTAMP, String.valueOf(now))
                .header(CloudSignature.HEADER_SIGNATURE, CloudSignature.sign(TOKEN, "GET", target, now, null, null, new byte[0]))
                .header("erupt", "Item").build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode(), resp.body());
        return resp.body();
    }
}
