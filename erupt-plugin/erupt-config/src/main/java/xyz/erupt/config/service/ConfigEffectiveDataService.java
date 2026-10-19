package xyz.erupt.config.service;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import xyz.erupt.config.model.ConfigEffectiveValue;
import xyz.erupt.config.boot.ConfigValues;
import xyz.erupt.config.view.ConfigValueHtml;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.service.EruptBeanDataService;
import xyz.erupt.core.view.EruptModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The effective values page, read from the running property source: no query, no table.
 *
 * @author YuePeng
 */
@Service
public class ConfigEffectiveDataService extends EruptBeanDataService<ConfigEffectiveValue> {

    public static final String DATA_PROCESSOR = "ERUPT_CONFIG_EFFECTIVE";

    static {
        DataProcessorManager.register(DATA_PROCESSOR, ConfigEffectiveDataService.class);
    }

    private final EruptConfigService configService;

    private final Environment environment;

    public ConfigEffectiveDataService(EruptConfigService configService, Environment environment) {
        this.configService = configService;
        this.environment = environment;
    }

    @Override
    protected List<ConfigEffectiveValue> data(EruptModel eruptModel, EruptQuery eruptQuery) {
        List<ConfigEffectiveValue> rows = new ArrayList<>();
        for (Map.Entry<String, EruptConfigService.Origin> entry : configService.origins().entrySet()) {
            ConfigEffectiveValue row = new ConfigEffectiveValue();
            row.setId(entry.getKey());
            row.setKey(entry.getKey());
            // what the environment answers, so a placeholder inside a value shows resolved; a flattened
            // entry of a structured item is plain text, the item itself keeps its type's look
            String value = environment.getProperty(entry.getKey());
            String type = entry.getValue().item().equals(entry.getKey()) ? entry.getValue().type() : ConfigValues.STRING;
            row.setValue(ConfigValueHtml.render(type, value, entry.getValue().secret()));
            row.setSource(entry.getValue().profile());
            row.setType(entry.getValue().type());
            rows.add(row);
        }
        return rows;
    }

}
