package xyz.erupt.config.handler;

import org.springframework.stereotype.Component;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.config.service.EruptConfigService;

import java.util.List;

/**
 * The Refresh now button of the items and the effective values: reloads the values from the
 * database (every instance, with Redis sessions) and answers how many keys changed here.
 *
 * @author YuePeng
 */
@Component
public class ConfigRefreshHandler implements OperationHandler<Object, Void> {

    private final EruptConfigService configService;

    public ConfigRefreshHandler(EruptConfigService configService) {
        this.configService = configService;
    }

    @Override
    public String exec(List<Object> data, Void form, String[] param) {
        int changed = configService.refresh().size();
        return message(0 == changed ? I18nTranslate.$translate("config.refreshed_none")
                : I18nTranslate.$translate("config.refreshed").replace("{n}", String.valueOf(changed)));
    }

    /** A success toast carrying the text as a JS string literal. */
    public static String message(String text) {
        return "msg.success(" + GsonFactory.getGson().toJson(text) + ")";
    }

}
