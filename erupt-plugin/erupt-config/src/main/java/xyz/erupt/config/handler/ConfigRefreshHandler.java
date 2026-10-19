package xyz.erupt.config.handler;

import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.config.model.ConfigEffectiveValue;
import xyz.erupt.config.service.EruptConfigService;

import java.util.List;

/**
 * Reloads this instance's values from the database and answers how many keys changed.
 *
 * @author YuePeng
 */
@Component
public class ConfigRefreshHandler implements OperationHandler<ConfigEffectiveValue, Void> {

    private final EruptConfigService configService;

    public ConfigRefreshHandler(EruptConfigService configService) {
        this.configService = configService;
    }

    @Override
    public String exec(List<ConfigEffectiveValue> data, Void form, String[] param) {
        return "msg.success('" + configService.refresh().size() + "')";
    }

}
