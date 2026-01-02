package xyz.erupt.annotation.sub_field.sub_edit;

import org.intellij.lang.annotations.Language;
import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.config.Match;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.ChoiceTrigger;

import java.beans.Transient;

/**
 * @author YuePeng
 * date 2018-10-09.
 */
public @interface ChoiceType {

    Type type() default Type.SELECT;

    @Transient
    @Comment("手动下拉列表")
    VL[] vl() default {};

    @Transient
    @Comment("可被fetchHandler接口获取到")
    String[] fetchHandlerParams() default {};

    @Transient
    @Comment("动态下拉列表")
    Class<? extends ChoiceFetchHandler>[] fetchHandler() default {};

    @Transient
    @Comment("可被trigger接口获取到")
    String[] triggerParams() default {};

    @Comment("选择数据时触发动作")
    @Match("#item.trigger().getSimpleName() != 'ChoiceTrigger'")
    Class<? extends ChoiceTrigger> trigger() default ChoiceTrigger.class;

    @Comment("开启后在编辑等操作时会重新获取下拉列表")
    boolean anewFetch() default false;

    //联动能力
    @Comment("联动能力，依赖字段名")
    @Language(value = "java", prefix = "private String ", suffix = ";")
    String dependField() default "";

    enum Type {
        @Comment("下拉选择")
        SELECT,
        @Comment("单选框")
        RADIO,
    }
}