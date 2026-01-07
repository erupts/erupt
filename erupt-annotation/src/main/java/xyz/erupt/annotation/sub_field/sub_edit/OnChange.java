package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.config.Comment;

import java.util.Map;

/**
 * @author YuePeng
 * date 2026/1/2 22:19
 */
public interface OnChange<MODEL> {

    @Comment("根据用户输入填充表单")
    default Map<String, Object> populateForm(MODEL model, String[] params) {
        return null;
    }

    @Comment("根据用户输入动态调整不同的字段的 @Edit 注解配置")
    @Comment("demo: return Map.of(\"name\", \"edit.desc='xxxxx'\");")
    default Map<String, String> buildEditExpr(MODEL model, String[] params) {
        return null;
    }


}
