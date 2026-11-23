package xyz.erupt.upms.service;

import com.google.gson.JsonObject;
import jakarta.annotation.Resource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import xyz.erupt.core.service.EruptModifyGate;
import xyz.erupt.core.view.EruptModel;

import java.util.List;

/**
 * Read-only mode of the site settings: while it is on, no insert, update or delete reaches a table
 * and the user is told why; queries go on. Asked before every other gate, so a change is not sent
 * into an approval it could not be applied from. The site settings are a form view, which no gate
 * guards, so an administrator can always switch the mode off again.
 *
 * @author YuePeng
 * date 2026/11/23
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SiteReadOnlyGate implements EruptModifyGate {

    @Resource
    private EruptSiteSettingService eruptSiteSettingService;

    @Override
    public String add(EruptModel eruptModel, JsonObject data) {
        return this.refusal();
    }

    @Override
    public String update(EruptModel eruptModel, JsonObject data) {
        return this.refusal();
    }

    @Override
    public String delete(EruptModel eruptModel, List<Object> ids) {
        return this.refusal();
    }

    private String refusal() {
        return eruptSiteSettingService.readOnlyActive() ? eruptSiteSettingService.readOnlyMessage() : null;
    }

}
