package xyz.erupt.jpa.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import xyz.erupt.annotation.model.Location;
import xyz.erupt.core.config.GsonFactory;

/**
 * Stores a {@link Location} as the JSON text the frontend map editor produces, so an
 * entity field can change from {@code String} to {@code Location} without touching the
 * column or its existing rows. Give the column room for a name and address, e.g.
 * {@code @Column(length = 500)}.
 *
 * @author YuePeng
 */
@Converter(autoApply = true)
public class LocationConverter implements AttributeConverter<Location, String> {

    @Override
    public String convertToDatabaseColumn(Location attribute) {
        return null == attribute ? null : GsonFactory.getGson().toJson(attribute);
    }

    @Override
    public Location convertToEntityAttribute(String dbData) {
        return null == dbData || dbData.isBlank() ? null : GsonFactory.getGson().fromJson(dbData, Location.class);
    }

}
