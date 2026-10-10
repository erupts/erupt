package xyz.erupt.test.webhook;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionTemplate;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.context.MetaUser;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.service.EruptModifyService;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.test.EruptApplicationTests;
import xyz.erupt.test.model.erupt.AuthVerifyModel;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.webhook.handler.EruptWebhookDataProxy;
import xyz.erupt.webhook.model.EruptWebhook;
import xyz.erupt.webhook.model.EruptWebhookLog;
import xyz.erupt.webhook.model.WebhookEvent;
import xyz.erupt.webhook.service.EruptWebhookService;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * erupt-webhook: a committed change on a subscribed model reaches the receiver as a signed JSON
 * payload, and every delivery leaves a log row.
 */
public class WebhookTest extends EruptApplicationTests {

    private static final String ERUPT = AuthVerifyModel.class.getSimpleName();

    private static final String SECRET = "s3cret";

    @Resource
    private EruptModifyService modifyService;

    @Resource
    private EruptWebhookService webhookService;

    @Resource
    private EruptWebhookDataProxy dataProxy;

    @Resource
    private TransactionTemplate transactionTemplate;

    private HttpServer server;

    private final List<String[]> received = new CopyOnWriteArrayList<>();

    private CountDownLatch latch;

    @BeforeEach
    void setUp() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/hook", exchange -> {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            received.add(new String[]{body, exchange.getRequestHeaders().getFirst("X-Erupt-Signature"), exchange.getRequestHeaders().getFirst("X-Token")});
            exchange.sendResponseHeaders(200, 2);
            exchange.getResponseBody().write("ok".getBytes(StandardCharsets.UTF_8));
            exchange.close();
            latch.countDown();
        });
        server.start();
        transactionTemplate.executeWithoutResult(status -> {
            EruptWebhook webhook = new EruptWebhook();
            webhook.setName("test-hook");
            webhook.setEnabled(true);
            webhook.setErupts(Set.of(ERUPT));
            webhook.setEvents(Set.of(WebhookEvent.ADD, WebhookEvent.UPDATE));
            webhook.setUrl("http://127.0.0.1:" + server.getAddress().getPort() + "/hook");
            webhook.setSecret(SECRET);
            webhook.setHeaders("{\"X-Token\":\"abc\"}");
            eruptDao.persist(webhook);
        });
        webhookService.refresh();
        MetaContext.register(new MetaUser(1L, "admin", "admin"));
    }

    @AfterEach
    void cleanUp() {
        server.stop(0);
        transactionTemplate.executeWithoutResult(status -> {
            eruptDao.lambdaQuery(AuthVerifyModel.class).eq(AuthVerifyModel::getKey, "hook-key").delete();
            eruptDao.lambdaQuery(EruptWebhookLog.class).eq(EruptWebhookLog::getWebhook, "test-hook").delete();
            eruptDao.lambdaQuery(EruptWebhook.class).eq(EruptWebhook::getName, "test-hook").list().forEach(it -> eruptDao.getEntityManager().remove(it));
        });
        webhookService.refresh();
        MetaContext.remove();
    }

    @Test
    void addIsDeliveredSignedAndLogged() throws Exception {
        latch = new CountDownLatch(1);
        JsonObject data = new JsonObject();
        data.addProperty("key", "hook-key");
        data.addProperty("value", "v1");
        Long id = (Long) modifyService.insertEruptData(this.model(), data);
        assertTrue(latch.await(10, TimeUnit.SECONDS), "receiver was not called");

        String body = received.get(0)[0];
        JsonObject payload = JsonParser.parseString(body).getAsJsonObject();
        assertEquals("ADD", payload.get("event").getAsString());
        assertEquals(ERUPT, payload.get("erupt").getAsString());
        assertEquals(String.valueOf(id), payload.get("recordId").getAsString());
        assertEquals("admin", payload.get("operator").getAsString());
        assertTrue(payload.get("before").isJsonNull());
        assertEquals("v1", payload.getAsJsonObject("after").get("value").getAsString());
        assertEquals("sha256=" + EruptWebhookService.sign(SECRET, body), received.get(0)[1]);
        assertEquals("abc", received.get(0)[2]);

        EruptWebhookLog log = this.awaitLog();
        assertTrue(log.getSuccess());
        assertEquals(200, log.getStatus());
        assertEquals(1, log.getAttempts());
        assertEquals(WebhookEvent.ADD, log.getEvent());
        assertEquals(String.valueOf(id), log.getRecordId());
    }

    @Test
    void deleteIsNotSubscribed() throws Exception {
        latch = new CountDownLatch(1);
        JsonObject data = new JsonObject();
        data.addProperty("key", "hook-key");
        data.addProperty("value", "v1");
        Long id = (Long) modifyService.insertEruptData(this.model(), data);
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        latch = new CountDownLatch(1);
        modifyService.deleteEruptData(this.model(), List.of(id), false);
        assertFalse(latch.await(2, TimeUnit.SECONDS), "DELETE is not subscribed and must not be delivered");
        assertEquals(1, received.size());
    }

    @Test
    void pingReportsTheReceiverAnswer() {
        latch = new CountDownLatch(1);
        EruptWebhook webhook = eruptDao.lambdaQuery(EruptWebhook.class).eq(EruptWebhook::getName, "test-hook").one();
        EruptWebhookService.Delivery delivery = webhookService.ping(webhook);
        assertTrue(delivery.success());
        assertEquals(200, delivery.status());
        assertEquals("ok", delivery.response());
        assertEquals("PING", JsonParser.parseString(received.get(0)[0]).getAsJsonObject().get("event").getAsString());
    }

    @Test
    void allModelsSwitchSubscribesEveryModel() throws Exception {
        transactionTemplate.executeWithoutResult(status -> {
            EruptWebhook webhook = eruptDao.lambdaQuery(EruptWebhook.class).eq(EruptWebhook::getName, "test-hook").one();
            webhook.setAllModels(true);
            webhook.setErupts(new HashSet<>());
            eruptDao.merge(webhook);
        });
        webhookService.refresh();
        latch = new CountDownLatch(1);
        JsonObject data = new JsonObject();
        data.addProperty("key", "hook-key");
        data.addProperty("value", "v1");
        modifyService.insertEruptData(this.model(), data);
        assertTrue(latch.await(10, TimeUnit.SECONDS), "a wildcard subscription must receive every model");
        assertEquals(ERUPT, JsonParser.parseString(received.get(0)[0]).getAsJsonObject().get("erupt").getAsString());
    }

    @Test
    void resendPostsTheStoredPayloadAgain() throws Exception {
        latch = new CountDownLatch(1);
        JsonObject data = new JsonObject();
        data.addProperty("key", "hook-key");
        data.addProperty("value", "v1");
        modifyService.insertEruptData(this.model(), data);
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        EruptWebhookLog first = this.awaitLog();
        assertNotNull(first.getWebhookId());

        latch = new CountDownLatch(1);
        webhookService.redeliver(first);
        assertTrue(latch.await(10, TimeUnit.SECONDS), "resend was not delivered");
        assertEquals(received.get(0)[0], received.get(1)[0], "resend carries the stored payload unchanged");
        for (int i = 0; i < 50 && eruptDao.lambdaQuery(EruptWebhookLog.class).eq(EruptWebhookLog::getWebhook, "test-hook").list().size() < 2; i++) {
            Thread.sleep(100);
        }
        assertEquals(2, eruptDao.lambdaQuery(EruptWebhookLog.class).eq(EruptWebhookLog::getWebhook, "test-hook").list().size());
    }

    // the list query leaves collections out; the proxy summarises them into the tag columns
    @Test
    void tableRowsCarryTheSubscriptionAsTags() {
        EruptWebhook webhook = eruptDao.lambdaQuery(EruptWebhook.class).eq(EruptWebhook::getName, "test-hook").one();
        Map<String, Object> row = new HashMap<>(Map.of("id", webhook.getId(), "name", "test-hook"));
        dataProxy.afterFetch(List.of(row));
        List<String> models = JsonParser.parseString((String) row.get("modelTags")).getAsJsonArray().asList().stream().map(it -> it.getAsString()).toList();
        List<String> events = JsonParser.parseString((String) row.get("eventTags")).getAsJsonArray().asList().stream().map(it -> it.getAsString()).toList();
        assertEquals(List.of(this.model().getErupt().name()), models);
        assertEquals(2, events.size());

        // a new form starts with every event ticked
        Map<String, Object> fresh = EruptUtil.generateEruptDataMap(EruptCoreService.getErupt(EruptWebhook.class.getSimpleName()), new EruptWebhook(), false);
        assertEquals(WebhookEvent.values().length, ((Collection<?>) fresh.get("events")).size());
    }

    // the form submits both sets as JSON arrays; they land in their JSON columns and read back typed
    @Test
    void formSubmissionStoresBothSetsAsJson() {
        JsonObject data = new JsonObject();
        data.addProperty("name", "test-hook-form");
        data.addProperty("enabled", false);
        data.addProperty("url", "http://127.0.0.1:1/none");
        data.addProperty("allModels", false);
        JsonArray erupts = new JsonArray();
        erupts.add(ERUPT);
        data.add("erupts", erupts);
        JsonArray events = new JsonArray();
        events.add("DELETE");
        data.add("events", events);
        Object id = modifyService.insertEruptData(EruptCoreService.getErupt(EruptWebhook.class.getSimpleName()), data);
        try {
            EruptWebhook saved = eruptDao.lambdaQuery(EruptWebhook.class).eq(EruptWebhook::getId, id).one();
            assertEquals(Set.of(ERUPT), saved.getErupts());
            assertEquals(Set.of(WebhookEvent.DELETE), saved.getEvents());
        } finally {
            transactionTemplate.executeWithoutResult(status -> eruptDao.lambdaQuery(EruptWebhook.class).eq(EruptWebhook::getId, id).delete());
        }
    }

    private EruptWebhookLog awaitLog() throws InterruptedException {
        for (int i = 0; i < 50; i++) {
            List<EruptWebhookLog> logs = eruptDao.lambdaQuery(EruptWebhookLog.class).eq(EruptWebhookLog::getWebhook, "test-hook").list();
            if (!logs.isEmpty()) return logs.get(0);
            Thread.sleep(100);
        }
        return fail("no delivery log was written");
    }

    private EruptModel model() {
        return EruptCoreService.getErupt(ERUPT);
    }

}
