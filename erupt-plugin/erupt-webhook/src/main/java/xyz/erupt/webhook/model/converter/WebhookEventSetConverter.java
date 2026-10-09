package xyz.erupt.webhook.model.converter;

import com.google.gson.reflect.TypeToken;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.apache.commons.lang3.StringUtils;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.webhook.model.WebhookEvent;

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Stores the subscribed events as a JSON array of names in one column: three constants at most,
 * always read whole and never queried by SQL, so a side table would be overhead.
 */
@Converter
public class WebhookEventSetConverter implements AttributeConverter<Set<WebhookEvent>, String> {

    @Override
    public String convertToDatabaseColumn(Set<WebhookEvent> attribute) {
        return null == attribute || attribute.isEmpty() ? null : GsonFactory.getGson().toJson(attribute);
    }

    @Override
    public Set<WebhookEvent> convertToEntityAttribute(String dbData) {
        if (StringUtils.isBlank(dbData)) return EnumSet.noneOf(WebhookEvent.class);
        return GsonFactory.getGson().fromJson(dbData, new TypeToken<LinkedHashSet<WebhookEvent>>() {
        }.getType());
    }

}
