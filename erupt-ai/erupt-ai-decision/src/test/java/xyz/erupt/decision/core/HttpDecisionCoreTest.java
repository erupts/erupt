package xyz.erupt.decision.core;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.decision.Decision;
import xyz.erupt.decision.model.DecisionModel;
import xyz.erupt.decision.question.Noul;
import xyz.erupt.decision.question.Question;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Posts to a System One endpoint standing in this process and checks what arrives: a hosted
 * provider must be keyed, a self-hosted one may not be, and the body is what Laya's sidecar
 * and Jev both answer with.
 *
 * @author YuePeng
 */
public class HttpDecisionCoreTest {

    private static final String RESPONSE = """
            {
              "model": "laya-rl-agent",
              "answers": {
                "test": { "type": "noul", "noul": 0.97, "confidence": 0.97,
                          "action": { "act_probability": 0.9 } }
              },
              "usage": { "input_tokens": 42, "output_tokens": 0 },
              "routing": { "model": "english", "reason": "English Latin text" }
            }
            """;

    private static HttpServer server;

    private static volatile String lastAuthorization;

    private static volatile JsonObject lastRequest;

    private static volatile String lastUpgrade;

    @BeforeAll
    static void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/systemone", exchange -> {
            lastAuthorization = exchange.getRequestHeaders().getFirst("Authorization");
            lastUpgrade = exchange.getRequestHeaders().getFirst("Upgrade");
            lastRequest = JsonParser.parseString(new String(exchange.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8)).getAsJsonObject();
            byte[] body = RESPONSE.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        });
        server.start();
    }

    @AfterAll
    static void stop() {
        server.stop(0);
    }

    private static DecisionModel config(DecisionCore core, String key) {
        DecisionModel config = new DecisionModel();
        config.setName(core.code());
        config.setProvider(core.code());
        config.setModel(core.model());
        config.setApiUrl("http://127.0.0.1:" + server.getAddress().getPort());
        config.setApiKey(key);
        config.setRetries(0);
        return config;
    }

    private static Map<String, Question<?>> question() {
        return Map.of("test", Noul.of("Is this sentence written in English?"));
    }

    @Test
    public void layaGetsByWithoutAKeyAndIgnoresTheExtraKeysItAnswersWith() {
        Laya laya = new Laya();
        Decision decision = laya.evaluate(config(laya, null), "The quick brown fox.", question());

        assertNull(lastAuthorization);
        // No h2c handshake on plain http: uvicorn drops the body of an upgrade request
        assertNull(lastUpgrade);
        assertEquals("auto", lastRequest.get("model").getAsString());
        assertEquals("laya-rl-agent", decision.model());
        assertEquals(42, decision.usage().inputTokens());
        assertTrue(decision.noul("test").yes(0.9));
    }

    @Test
    public void aKeyIsSentAsBearerWhenThereIsOne() {
        Laya laya = new Laya();
        laya.evaluate(config(laya, "sidecar-secret"), "The quick brown fox.", question());

        assertEquals("Bearer sidecar-secret", lastAuthorization);
    }

    @Test
    public void jevRefusesToPostWithoutAKey() {
        Jev jev = new Jev();
        assertThrows(EruptWebApiRuntimeException.class,
                () -> jev.evaluate(config(jev, " "), "The quick brown fox.", question()));
    }

    @Test
    public void theEndpointPathIsAddedOnceWhateverTheDomainLooksLike() {
        assertEquals("https://api.typesafe.ai/v1/systemone", HttpDecisionCore.endpoint("https://api.typesafe.ai/"));
        assertEquals("http://127.0.0.1:8000/v1/systemone", HttpDecisionCore.endpoint("http://127.0.0.1:8000/v1/systemone"));
        assertFalse(HttpDecisionCore.endpoint("http://host").endsWith("//v1/systemone"));
    }

}
