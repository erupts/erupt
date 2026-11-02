package xyz.erupt.config.proxy;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.config.boot.ConfigValues;
import xyz.erupt.config.model.ConfigItem;
import xyz.erupt.config.model.ConfigProfile;
import xyz.erupt.config.service.EruptConfigService;
import xyz.erupt.config.view.ConfigValueHtml;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.context.OldEntityTL;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.util.DateUtil;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.linq.lambda.LambdaSee;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * One key per profile, the editor of the item's type reading and writing the stored value,
 * secrets masked wherever text leaves the server, and the running values refreshed on every write.
 *
 * @author YuePeng
 */
@Component
public class ConfigItemDataProxy implements DataProxy<ConfigItem> {

    // Editors that can show the mask in place of a secret; the others (number, date, file...) have no way to
    private static final Set<String> MASKABLE = Set.of(ConfigValues.STRING, ConfigValues.PASSWORD, ConfigValues.TEXT, ConfigValues.LIST,
            ConfigValues.JSON, ConfigValues.YAML, ConfigValues.HTML);

    private final EruptDao eruptDao;

    private final EruptConfigService configService;

    public ConfigItemDataProxy(EruptDao eruptDao, EruptConfigService configService) {
        this.eruptDao = eruptDao;
        this.configService = configService;
    }

    @Override
    public void beforeAdd(ConfigItem item) {
        this.unique(item);
        this.store(item);
    }

    @Override
    public void beforeUpdate(ConfigItem item) {
        this.unique(item);
        this.store(item);
        // the mask posted back unchanged keeps the stored value; erupt's own PASSWORD handling already
        // drops its placeholder before we see it, which leaves the editor empty. The item is the managed
        // entity, already merged, so the previous value comes from erupt's snapshot of it
        if (ConfigItem.MASK.equals(item.getValue()) || (ConfigValues.PASSWORD.equals(item.getType()) && null == item.getValue())) {
            JsonElement previous = JsonParser.parseString(OldEntityTL.get()).getAsJsonObject().get(LambdaSee.field(ConfigItem::getValue));
            item.setValue(null == previous || previous.isJsonNull() ? null : previous.getAsString());
        }
    }

    // The editor of the item's type writes the stored value
    private void store(ConfigItem item) {
        String type = null == item.getType() ? ConfigValues.STRING : item.getType();
        if (ConfigValues.PASSWORD.equals(type)) item.setSecret(true);
        switch (type) {
            case ConfigValues.PASSWORD -> item.setValue(item.getPasswordValue());
            case ConfigValues.TEXT -> item.setValue(item.getTextValue());
            case ConfigValues.NUMBER -> item.setValue(null == item.getNumberValue() ? null
                    : item.getNumberValue() == Math.rint(item.getNumberValue()) ? String.valueOf(item.getNumberValue().longValue()) : String.valueOf(item.getNumberValue()));
            case ConfigValues.BOOLEAN -> item.setValue(String.valueOf(Boolean.TRUE.equals(item.getBoolValue())));
            case ConfigValues.LIST -> item.setValue(item.getListValue());
            case ConfigValues.KEY_VALUE -> item.setValue(GsonFactory.getGson().toJson(null == item.getMapValue() ? Map.of() : item.getMapValue()));
            case ConfigValues.JSON -> {
                if (!ConfigItem.MASK.equals(item.getJsonValue()) && null == ConfigValues.parseJson(item.getJsonValue())) {
                    throw new EruptWebApiRuntimeException(I18nTranslate.$translate("config.json_invalid"));
                }
                item.setValue(item.getJsonValue());
            }
            case ConfigValues.YAML -> {
                if (!ConfigItem.MASK.equals(item.getYamlValue()) && null == ConfigValues.parseYaml(item.getYamlValue())) {
                    throw new EruptWebApiRuntimeException(I18nTranslate.$translate("config.yaml_invalid"));
                }
                item.setValue(item.getYamlValue());
            }
            case ConfigValues.DATE -> item.setValue(null == item.getDateValue() ? null : item.getDateValue().toString());
            case ConfigValues.DATETIME -> item.setValue(null == item.getDateTimeValue() ? null : item.getDateTimeValue().toString());
            case ConfigValues.TIME -> item.setValue(item.getTimeValue());
            case ConfigValues.COLOR -> item.setValue(item.getColorValue());
            case ConfigValues.ICON -> item.setValue(item.getIconValue());
            case ConfigValues.IMAGE -> item.setValue(item.getImageValue());
            case ConfigValues.HTML -> item.setValue(item.getHtmlValue());
            case ConfigValues.ATTACHMENT -> item.setValue(item.getFileValue());
            default -> {
            }
        }
    }

    // The stored value fills the editor of the item's type when the form opens; a secret shows the mask instead
    @Override
    public void editBehavior(ConfigItem item) {
        String type = null == item.getType() ? ConfigValues.STRING : item.getType();
        String value = Boolean.TRUE.equals(item.getSecret()) && MASKABLE.contains(type) ? ConfigItem.MASK : item.getValue();
        item.setValue(value);
        switch (type) {
            case ConfigValues.PASSWORD -> item.setPasswordValue(value);
            case ConfigValues.TEXT -> item.setTextValue(value);
            case ConfigValues.NUMBER -> item.setNumberValue(null == value || value.isBlank() ? null : Double.valueOf(value));
            case ConfigValues.BOOLEAN -> item.setBoolValue(Boolean.parseBoolean(value));
            case ConfigValues.LIST -> item.setListValue(value);
            case ConfigValues.KEY_VALUE -> {
                JsonElement element = ConfigValues.parseJson(value);
                Map<String, String> map = new LinkedHashMap<>();
                if (null != element && element.isJsonObject()) {
                    element.getAsJsonObject().entrySet().forEach(e -> map.put(e.getKey(), e.getValue().isJsonPrimitive() ? e.getValue().getAsString() : e.getValue().toString()));
                }
                item.setMapValue(map);
            }
            case ConfigValues.JSON -> item.setJsonValue(value);
            case ConfigValues.YAML -> item.setYamlValue(value);
            case ConfigValues.DATE -> item.setDateValue(null == value || value.isBlank() ? null : DateUtil.parseLocalDate(value));
            case ConfigValues.DATETIME -> item.setDateTimeValue(null == value || value.isBlank() ? null : DateUtil.parseLocalDateTime(value));
            case ConfigValues.TIME -> item.setTimeValue(value);
            case ConfigValues.COLOR -> item.setColorValue(value);
            case ConfigValues.ICON -> item.setIconValue(value);
            case ConfigValues.IMAGE -> item.setImageValue(value);
            case ConfigValues.HTML -> item.setHtmlValue(value);
            case ConfigValues.ATTACHMENT -> item.setFileValue(value);
            default -> {
            }
        }
    }

    private void unique(ConfigItem item) {
        long same = eruptDao.lambdaQuery(ConfigItem.class)
                .with(ConfigItem::getProfile).eq(ConfigProfile::getId, item.getProfile().getId()).with()
                .eq(ConfigItem::getKey, item.getKey()).ne(null != item.getId(), ConfigItem::getId, item.getId()).count();
        if (same > 0) throw new EruptWebApiRuntimeException(I18nTranslate.$translate("config.key_exists") + " → " + item.getKey());
    }

    @Override
    public void afterAdd(ConfigItem item) {
        configService.refresh();
    }

    @Override
    public void afterUpdate(ConfigItem item) {
        configService.refresh();
    }

    @Override
    public void afterDelete(ConfigItem item) {
        configService.refresh();
    }

    // The cell shows the value the way its type reads best; a secret shows the mask
    @Override
    public void afterFetch(Collection<Map<String, Object>> list) {
        String value = LambdaSee.field(ConfigItem::getValue);
        for (Map<String, Object> row : list) {
            row.put(value, ConfigValueHtml.render(String.valueOf(row.get(LambdaSee.field(ConfigItem::getType))),
                    null == row.get(value) ? null : String.valueOf(row.get(value)), Boolean.TRUE.equals(row.get(LambdaSee.field(ConfigItem::getSecret)))));
        }
    }

}
