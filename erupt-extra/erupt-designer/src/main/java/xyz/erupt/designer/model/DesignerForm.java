package xyz.erupt.designer.model;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Design produced by the visual form designer.
 * <p>
 * The {@code erupt} / {@code view} / {@code edit} json objects mirror the structure of the
 * {@link xyz.erupt.annotation.Erupt} / {@link xyz.erupt.annotation.sub_field.View} /
 * {@link xyz.erupt.annotation.sub_field.Edit} annotations (raw member names), so the backend
 * can convert them to annotation source code generically by reflection.
 *
 * @author YuePeng
 * date 2026-06-12
 */
@Getter
@Setter
public class DesignerForm {

    private String pkg;

    private String className;

    private String tableName;

    // BaseModel / MetaModel / empty
    private String extendsModel;

    // @Erupt annotation structure
    private JsonObject erupt;

    private List<DesignerField> fields;

    @Getter
    @Setter
    public static class DesignerField {

        private String fieldName;

        // explicit java type, e.g. Integer / Long / Double / BigDecimal
        private String fieldType;

        // linked @Erupt class name for reference / tab types
        private String linkErupt;

        // @View annotation structure (nullable → no table column)
        private JsonObject view;

        // @Edit annotation structure
        private JsonObject edit;
    }

}
