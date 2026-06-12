package xyz.erupt.print.pojo;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.core.config.GsonFactory;

/**
 * @author YuePeng
 * date 2026/1/27 22:42
 */
@Getter
@Setter
@Converter
public class PrintPageConfig implements AttributeConverter<PrintPageConfig, String> {

    private String paperSize;

    private String orientation;

    private Integer marginTop;

    private Integer marginBottom;

    private Integer marginLeft;

    private Integer marginRight;

    @Override
    public String convertToDatabaseColumn(PrintPageConfig printPageConfig) {
        return GsonFactory.getGson().toJson(printPageConfig);
    }

    @Override
    public PrintPageConfig convertToEntityAttribute(String dbData) {
        return GsonFactory.getGson().fromJson(dbData, PrintPageConfig.class);
    }
}
