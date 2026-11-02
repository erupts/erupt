package xyz.erupt.webhook.handler;

import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.webhook.service.EruptWebhookService;

import java.util.List;

/**
 * Every registered model a webhook may subscribe to, valued by erupt name and labelled by its raw
 * {@code @Erupt(name)}; the owning model is {@code @EruptI18n}, so the labels are translated afterwards.
 */
@Component
public class WebhookEruptChoice implements ChoiceFetchHandler<Void> {

    @Override
    public List<VLModel> fetch(String[] params) {
        return EruptCoreService.getErupts().stream()
                .filter(it -> !it.isRemote() && EruptWebhookService.subscribable(it.getClazz()))
                .map(it -> new VLModel(it.getEruptName(), it.getErupt().name()))
                .toList();
    }

}
