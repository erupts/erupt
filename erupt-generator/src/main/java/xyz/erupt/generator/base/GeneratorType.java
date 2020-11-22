package xyz.erupt.generator.base;

import lombok.Getter;
import xyz.erupt.annotation.sub_field.EditType;

import java.util.Date;

@Getter
public enum GeneratorType {
    INPUT(EditType.INPUT, "输入框", String.class.getSimpleName(), "inputType = @InputType"),
    NUMBER(EditType.NUMBER, "数值框", Integer.class.getSimpleName(), "numberType = @NumberType"),
    SLIDER(EditType.SLIDER, "数字滑块", Integer.class.getSimpleName(), "sliderType = @SliderType(max = 999)"),
    DATE(EditType.DATE, "日期", Date.class.getSimpleName(), "dateType = @DateType()"),
    DATE_TIME(EditType.DATE, "日期时间", Date.class.getSimpleName(), "dateType = @DateType(type = DateType.Type.DATE_TIME)"),
    MONTH(EditType.DATE, "月", String.class.getSimpleName(), "dateType = @DateType(type = DateType.Type.MONTH)"),
    BOOLEAN(EditType.BOOLEAN, "布尔开关", Boolean.class.getSimpleName(), "boolType = @BoolType"),
//    CHOICE("选择框", null),
//    TAGS("标签选择器", null),
//    ATTACHMENT("附件", null),
//    IMAGE("图片", ATTACHMENT),
//    AUTO_COMPLETE("自动联想", null),
//    HTML_EDITOR("富文本编辑器", null),
//    TEXTAREA("大文本域", null),
//    MAP("地图", null),
//    CODE_EDITOR("代码编辑器", null),
//    REFERENCE_TREE(null, null),
//    REFERENCE_TABLE(null, null),
//    CHECKBOX(null, null),
//    TAB_TREE(null, null),
//    TAB_TABLE_REFER(null, null),
//    TAB_TABLE_ADD(null, null),
//    TPL(null, null),
//    DIVIDE(null, null),
//    HIDDEN(null, null),
//    EMPTY(null, null)
    ;

    private final EditType mapping;

    private final String name;

    private final String type;

    private final String code;

    GeneratorType(EditType mapping, String name, String type, String code) {
        this.mapping = mapping;
        this.name = name;
        this.type = type;
        this.code = code;
    }
}
