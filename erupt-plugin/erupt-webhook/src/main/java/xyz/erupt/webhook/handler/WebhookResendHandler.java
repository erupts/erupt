package xyz.erupt.webhook.handler;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.webhook.model.EruptWebhookLog;
import xyz.erupt.webhook.service.EruptWebhookService;

import java.util.List;

/**
 * "Resend" on the log table: the stored payload goes out again through the delivery worker, as a new
 * log row, so the user refreshes the list for the outcome rather than waiting on the receiver.
 */
@Component
public class WebhookResendHandler implements OperationHandler<EruptWebhookLog, Void> {

    @Resource
    private EruptWebhookService webhookService;

    @Override
    public String exec(List<EruptWebhookLog> data, Void form, String[] param) {
        data.forEach(webhookService::redeliver);
        return EruptWebhookService.toast(String.format(I18nTranslate.$translate("webhook.resend_queued"), data.size()));
    }

}
