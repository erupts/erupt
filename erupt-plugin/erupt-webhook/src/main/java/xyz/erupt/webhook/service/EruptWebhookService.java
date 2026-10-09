package xyz.erupt.webhook.service;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionTemplate;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.context.MetaUser;
import xyz.erupt.core.event.EruptAddEvent;
import xyz.erupt.core.event.EruptDeleteEvent;
import xyz.erupt.core.event.EruptEditEvent;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.webhook.model.EruptWebhook;
import xyz.erupt.webhook.model.EruptWebhookLog;
import xyz.erupt.webhook.model.WebhookEvent;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Listens to the pipeline's data events once their transaction has committed, matches them against
 * the enabled subscriptions and hands each match to a delivery worker: a signed JSON POST with a
 * couple of retries, logged whatever the outcome. The request thread never waits for a receiver.
 */
@Slf4j
@Service
public class EruptWebhookService {

    private static final Gson GSON = GsonFactory.getGson();

    private static final int ATTEMPTS = 3;

    private static final long[] BACKOFF_MS = {1_000, 5_000};

    private static final int RESPONSE_LIMIT = 2_000;

    // Pinned to HTTP/1.1: over plain http the default h2c upgrade confuses some receivers
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ExecutorService executor = Executors.newFixedThreadPool(2, runnable -> {
        Thread thread = new Thread(runnable, "erupt-webhook");
        thread.setDaemon(true);
        return thread;
    });

    @Resource
    private EruptDao eruptDao;

    // the delivery worker has no request transaction of its own
    @Resource
    private TransactionTemplate transactionTemplate;

    // enabled subscriptions, reloaded after any change to the table
    private volatile List<EruptWebhook> enabled;

    public record Delivery(boolean success, int status, String response, int attempts, long duration) {
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onAdd(EruptAddEvent<Object> event) {
        this.dispatch(event.getEruptClass(), WebhookEvent.ADD, null, event.getSource());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onEdit(EruptEditEvent<Object> event) {
        this.dispatch(event.getEruptClass(), WebhookEvent.UPDATE, event.getBefore(), event.getSource());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onDelete(EruptDeleteEvent<Object> event) {
        this.dispatch(event.getEruptClass(), WebhookEvent.DELETE, event.getSource(), null);
    }

    public void refresh() {
        this.enabled = null;
    }

    // Sends a synthetic payload so an administrator can see the receiver answer before relying on it
    public Delivery ping(EruptWebhook webhook) {
        JsonObject payload = this.envelope("PING", null, null);
        payload.addProperty("message", "erupt webhook test");
        return this.deliver(webhook, payload.toString());
    }

    // Posts the stored payload of a logged delivery again, as a new delivery of the same event
    public void redeliver(EruptWebhookLog previous) {
        EruptWebhook webhook = null == previous.getWebhookId() ? null
                : eruptDao.lambdaQuery(EruptWebhook.class).eq(EruptWebhook::getId, previous.getWebhookId()).one();
        if (null == webhook) throw new EruptWebApiRuntimeException(I18nTranslate.$translate("webhook.gone"));
        String body = previous.getPayload();
        executor.execute(() -> this.record(webhook, previous.getErupt(), previous.getEvent(), previous.getRecordId(), body, this.deliver(webhook, body)));
    }

    // The module's own two tables never fire webhooks, not even for a subscription to every model
    public static boolean subscribable(Class<?> clazz) {
        return null != clazz && EruptWebhook.class != clazz && EruptWebhookLog.class != clazz;
    }

    // JS a handler returns: the frontend's global window.msg shows the text as a toast
    public static String toast(String text) {
        return "msg.success(" + GSON.toJson(text) + ")";
    }

    private void dispatch(Class<?> clazz, WebhookEvent event, Object before, Object after) {
        if (!subscribable(clazz)) return;
        List<EruptWebhook> matches = this.subscriptions().stream()
                .filter(it -> it.getEvents().contains(event) && this.subscribes(it, clazz.getSimpleName()))
                .toList();
        if (matches.isEmpty()) return;
        EruptModel eruptModel = this.resolve(clazz);
        if (null == eruptModel) return;
        try {
            // everything that depends on the request thread (user, masking) is captured here, before the handoff
            JsonElement beforeJson = this.masked(eruptModel, before);
            JsonElement afterJson = this.masked(eruptModel, after);
            String recordId = this.recordId(eruptModel, null == afterJson ? beforeJson : afterJson);
            JsonObject payload = this.envelope(event.name(), eruptModel, recordId);
            payload.add("before", beforeJson);
            payload.add("after", afterJson);
            String body = payload.toString();
            for (EruptWebhook webhook : matches) {
                executor.execute(() -> this.record(webhook, eruptModel.getEruptName(), event, recordId, body, this.deliver(webhook, body)));
            }
        } catch (Exception e) {
            // a webhook must never take the change itself down
            log.warn("erupt-webhook: failed to dispatch {} of {}: {}", event, clazz.getSimpleName(), e.getMessage());
        }
    }

    private boolean subscribes(EruptWebhook webhook, String erupt) {
        return Boolean.TRUE.equals(webhook.getAllModels()) || (null != webhook.getErupts() && webhook.getErupts().contains(erupt));
    }

    private List<EruptWebhook> subscriptions() {
        List<EruptWebhook> list = this.enabled;
        if (null == list) {
            list = eruptDao.lambdaQuery(EruptWebhook.class).eq(EruptWebhook::getEnabled, true).list();
            this.enabled = list;
        }
        return list;
    }

    private JsonObject envelope(String event, EruptModel eruptModel, String recordId) {
        JsonObject payload = new JsonObject();
        payload.addProperty("id", UUID.randomUUID().toString());
        payload.addProperty("event", event);
        if (null != eruptModel) {
            payload.addProperty("erupt", eruptModel.getEruptName());
            payload.addProperty("model", eruptModel.getErupt().name());
        }
        payload.addProperty("recordId", recordId);
        payload.addProperty("operator", Optional.ofNullable(MetaContext.getUser()).map(MetaUser::getAccount).orElse(null));
        payload.addProperty("timestamp", LocalDateTime.now(ZoneOffset.UTC).toString() + "Z");
        return payload;
    }

    private Delivery deliver(EruptWebhook webhook, String body) {
        long start = System.currentTimeMillis();
        int status = 0;
        String response = null;
        int attempt = 0;
        while (attempt < ATTEMPTS) {
            attempt++;
            try {
                HttpResponse<String> res = HTTP.send(this.request(webhook, body), HttpResponse.BodyHandlers.ofString());
                status = res.statusCode();
                response = this.clip(res.body());
                if (status >= 200 && status < 300) {
                    return new Delivery(true, status, response, attempt, System.currentTimeMillis() - start);
                }
            } catch (Exception e) {
                status = 0;
                response = this.clip(String.valueOf(e.getMessage()));
                if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            }
            if (attempt < ATTEMPTS) {
                try {
                    Thread.sleep(BACKOFF_MS[Math.min(attempt - 1, BACKOFF_MS.length - 1)]);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        return new Delivery(false, status, response, attempt, System.currentTimeMillis() - start);
    }

    private HttpRequest request(EruptWebhook webhook, String body) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(webhook.getUrl().trim()))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/json; charset=utf-8")
                .header("User-Agent", "erupt-webhook")
                .header("X-Erupt-Delivery", UUID.randomUUID().toString());
        if (null != webhook.getHeaders() && !webhook.getHeaders().isBlank()) {
            Map<String, String> headers = GSON.fromJson(webhook.getHeaders(), new com.google.gson.reflect.TypeToken<Map<String, String>>() {
            }.getType());
            headers.forEach((k, v) -> {
                if (null != k && !k.isBlank() && null != v) builder.header(k.trim(), v);
            });
        }
        if (null != webhook.getSecret() && !webhook.getSecret().isEmpty()) {
            builder.header("X-Erupt-Signature", "sha256=" + sign(webhook.getSecret(), body));
        }
        return builder.POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8)).build();
    }

    public static String sign(String secret, String body) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void record(EruptWebhook webhook, String erupt, WebhookEvent event, String recordId, String payload, Delivery delivery) {
        try {
            EruptWebhookLog log = new EruptWebhookLog();
            log.setWebhookId(webhook.getId());
            log.setWebhook(webhook.getName());
            log.setErupt(erupt);
            log.setEvent(event);
            log.setRecordId(recordId);
            log.setUrl(webhook.getUrl());
            log.setSuccess(delivery.success());
            log.setStatus(delivery.status());
            log.setAttempts(delivery.attempts());
            log.setDuration(delivery.duration());
            log.setResponse(delivery.response());
            log.setPayload(payload);
            log.setCreateTime(LocalDateTime.now());
            transactionTemplate.executeWithoutResult(status -> eruptDao.persist(log));
        } catch (Exception e) {
            EruptWebhookService.log.warn("erupt-webhook: failed to log delivery to {}: {}", webhook.getUrl(), e.getMessage());
        }
    }

    // Anything that is not a registered erupt is left alone.
    private EruptModel resolve(Class<?> clazz) {
        EruptModel eruptModel = EruptCoreService.getErupt(clazz.getSimpleName());
        if (null != eruptModel && eruptModel.getClazz() == clazz) return eruptModel;
        return EruptCoreService.getErupts().stream().filter(it -> it.getClazz() == clazz).findFirst().orElse(null);
    }

    private JsonElement masked(EruptModel eruptModel, Object obj) {
        return null == obj ? null : GSON.fromJson(EruptUtil.toMaskedJson(eruptModel, obj), JsonElement.class);
    }

    private String recordId(EruptModel eruptModel, JsonElement json) {
        if (null == json || !json.isJsonObject()) return null;
        JsonElement pk = json.getAsJsonObject().get(eruptModel.getErupt().primaryKeyCol());
        return null == pk || pk.isJsonNull() ? null : pk.getAsString();
    }

    private String clip(String text) {
        if (null == text) return null;
        return text.length() > RESPONSE_LIMIT ? text.substring(0, RESPONSE_LIMIT) : text;
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdown();
    }

}
