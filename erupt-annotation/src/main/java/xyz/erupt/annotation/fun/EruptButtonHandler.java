package xyz.erupt.annotation.fun;

import xyz.erupt.annotation.config.Comment;

import java.util.Map;

/**
 * @author YuePeng
 * date 2026-07-19
 */
public interface EruptButtonHandler<@Comment("erupt form object type") Erupt> {

    @Comment("Triggered when the form button is clicked, receives all current form values")
    @Comment("Return value: JS expression to be executed by the frontend after the event is triggered successfully; return null if not needed")
    default String click(Erupt erupt, String[] params) {
        return null;
    }

    @Comment("Populate the form based on all current form values")
    default Map<String, Object> populateForm(Erupt erupt, String[] params) {
        return null;
    }

    @Comment("Dynamically adjust the @Edit annotation configuration for different fields based on all current form values")
    @Comment("demo: return Map.of(\"name\", \"edit.desc='xxxxx'\");")
    default Map<String, String> buildEditExpr(Erupt erupt, String[] params) {
        return null;
    }

}
