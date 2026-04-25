package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.config.Comment;

import java.util.Map;

/**
 * @author YuePeng
 * date 2026/1/2 22:19
 */
public interface OnChange<MODEL> {

    @Comment("Populate the form based on user input")
    default Map<String, Object> populateForm(MODEL model, String[] params) {
        return null;
    }

    @Comment("Dynamically adjust the @Edit annotation configuration for different fields based on user input")
    @Comment("demo: return Map.of(\"name\", \"edit.desc='xxxxx'\");")
    default Map<String, String> buildEditExpr(MODEL model, String[] params) {
        return null;
    }


}
