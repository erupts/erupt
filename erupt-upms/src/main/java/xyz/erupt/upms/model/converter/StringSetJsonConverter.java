package xyz.erupt.upms.model.converter;

import com.google.gson.reflect.TypeToken;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.apache.commons.lang3.StringUtils;
import xyz.erupt.core.config.GsonFactory;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Stores a small string set as a JSON array in one column, so the owning entity needs
 * no side table. Only for sets that stay short and are always read whole.
 *
 * @author YuePeng
 * date 2026-09-18
 */
@Converter
public class StringSetJsonConverter implements AttributeConverter<Set<String>, String> {

    @Override
    public String convertToDatabaseColumn(Set<String> attribute) {
        return null == attribute || attribute.isEmpty() ? null : GsonFactory.getGson().toJson(attribute);
    }

    @Override
    public Set<String> convertToEntityAttribute(String dbData) {
        if (StringUtils.isBlank(dbData)) return new LinkedHashSet<>();
        return GsonFactory.getGson().fromJson(dbData, new TypeToken<LinkedHashSet<String>>() {
        }.getType());
    }

}
