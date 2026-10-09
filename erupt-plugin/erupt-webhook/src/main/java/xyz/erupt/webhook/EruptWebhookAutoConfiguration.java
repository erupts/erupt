package xyz.erupt.webhook;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import xyz.erupt.core.annotation.EruptScan;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.constant.MenuStatus;
import xyz.erupt.core.module.ModuleInfo;
import xyz.erupt.upms.EruptUpmsAutoConfiguration;
import xyz.erupt.webhook.model.EruptWebhook;
import xyz.erupt.webhook.model.EruptWebhookLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Outbound automation on the erupt data pipeline: an administrator subscribes a URL to the add,
 * update and delete events of chosen models, and every committed change is POSTed there as a
 * signed JSON payload, with retries and a delivery log. Opt-in by adding this module.
 *
 * @author YuePeng
 * date 2026/10/9
 */
@Configuration
@ComponentScan
@EruptScan
@EntityScan
public class EruptWebhookAutoConfiguration implements EruptModule {

    static {
        EruptModuleInvoke.addEruptModule(EruptWebhookAutoConfiguration.class);
    }

    public static final String ERUPT_WEBHOOK = "erupt-webhook";

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name(ERUPT_WEBHOOK).description("Outbound webhooks on data events").build();
    }

    @Override
    public List<MetaMenu> initMenus() {
        List<MetaMenu> menus = new ArrayList<>();
        MetaMenu manager = EruptUpmsAutoConfiguration.managerMenu();
        menus.add(manager);
        menus.add(MetaMenu.createEruptClassMenu(EruptWebhook.class, manager, 87));
        // reached through the "Deliveries" drill of a webhook; the hidden menu only grants the permission
        menus.add(MetaMenu.createEruptClassMenu(EruptWebhookLog.class, manager, 88, MenuStatus.HIDE));
        return menus;
    }

}
