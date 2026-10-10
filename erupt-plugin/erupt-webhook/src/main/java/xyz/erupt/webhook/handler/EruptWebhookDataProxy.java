package xyz.erupt.webhook.handler;

import com.google.gson.Gson;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.exception.EruptException;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.fun.EruptButtonHandler;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.webhook.model.EruptWebhook;
import xyz.erupt.webhook.model.WebhookEvent;
import xyz.erupt.webhook.service.EruptWebhookService;

import java.net.URI;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Backs every control of the webhook model: validates the URL, keeps the service's view of the
 * table in step, summarises each row's subscription into the tag columns, and sends the test ping
 * from both the form button (values as typed) and the row operation (the saved row).
 */
@Component
public class EruptWebhookDataProxy implements DataProxy<EruptWebhook>, OperationHandler<EruptWebhook, Void>, EruptButtonHandler<EruptWebhook> {

    private static final Gson GSON = GsonFactory.getGson();

    @Resource
    private EruptWebhookService webhookService;

    @Resource
    private EruptDao eruptDao;

    @Override
    public void validate(EruptWebhook webhook) throws EruptException {
        try {
            String scheme = URI.create(webhook.getUrl().trim()).getScheme();
            if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) throw new IllegalArgumentException();
        } catch (Exception e) {
            throw new EruptException(I18nTranslate.$translate("webhook.invalid_url"));
        }
        // the form only hides the picker behind the switch; the required check has to live here
        if (!Boolean.TRUE.equals(webhook.getAllModels()) && (null == webhook.getErupts() || webhook.getErupts().isEmpty())) {
            throw new EruptException(I18nTranslate.$translate("webhook.models_required"));
        }
    }

    @Override
    public void afterAdd(EruptWebhook webhook) {
        webhookService.refresh();
    }

    @Override
    public void afterUpdate(EruptWebhook webhook) {
        webhookService.refresh();
    }

    @Override
    public void afterDelete(EruptWebhook webhook) {
        webhookService.refresh();
    }

    // The list query leaves the two collections out; one query over the page fills the tag columns.
    @Override
    public void afterFetch(Collection<Map<String, Object>> list) {
        List<Object> ids = list.stream().map(it -> it.get(AnnotationConst.ID)).filter(java.util.Objects::nonNull).toList();
        if (ids.isEmpty()) return;
        Map<Long, EruptWebhook> byId = eruptDao.lambdaQuery(EruptWebhook.class).in(EruptWebhook::getId, ids).list()
                .stream().collect(Collectors.toMap(EruptWebhook::getId, Function.identity()));
        for (Map<String, Object> row : list) {
            EruptWebhook webhook = byId.get(((Number) row.get(AnnotationConst.ID)).longValue());
            if (null == webhook) continue;
            row.put("modelTags", GSON.toJson(this.modelLabels(webhook)));
            row.put("eventTags", GSON.toJson(this.eventLabels(webhook.getEvents())));
        }
    }

    private List<String> modelLabels(EruptWebhook webhook) {
        if (Boolean.TRUE.equals(webhook.getAllModels())) return List.of(I18nTranslate.$translate("webhook.all_models"));
        if (null == webhook.getErupts()) return List.of();
        return webhook.getErupts().stream().sorted().map(name -> Optional.ofNullable(EruptCoreService.getErupt(name))
                .map(EruptModel::getErupt).map(it -> I18nTranslate.$translate(it.name())).orElse(name)).toList();
    }

    private List<String> eventLabels(Set<WebhookEvent> events) {
        if (null == events) return List.of();
        return events.stream().sorted(Comparator.comparingInt(Enum::ordinal)).map(it -> I18nTranslate.$translate(it.name())).toList();
    }

    // Form button: ping with the values as typed; an edit form carries a placeholder for the secret
    @Override
    public String click(EruptWebhook form, String[] params) {
        try {
            this.validate(form);
        } catch (EruptException e) {
            throw new EruptWebApiRuntimeException(e.getMessage());
        }
        if (null != form.getId() && (null == form.getSecret() || form.getSecret().isBlank()
                || EruptConst.PASSWORD_PLACEHOLDER.equals(form.getSecret()))) {
            Optional.ofNullable(eruptDao.lambdaQuery(EruptWebhook.class).eq(EruptWebhook::getId, form.getId()).one())
                    .ifPresent(it -> form.setSecret(it.getSecret()));
        }
        return this.ping(form);
    }

    // Row operation: ping the saved row
    @Override
    public String exec(List<EruptWebhook> data, Void form, String[] param) {
        return this.ping(data.get(0));
    }

    private String ping(EruptWebhook webhook) {
        EruptWebhookService.Delivery delivery = webhookService.ping(webhook);
        if (!delivery.success()) {
            throw new EruptWebApiRuntimeException(String.format(I18nTranslate.$translate("webhook.ping_failed"), delivery.status(), delivery.response()));
        }
        return EruptWebhookService.toast(String.format(I18nTranslate.$translate("webhook.ping_ok"), delivery.status(), delivery.duration()));
    }

}
