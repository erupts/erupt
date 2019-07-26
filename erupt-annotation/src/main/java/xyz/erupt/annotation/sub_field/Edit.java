package xyz.erupt.annotation.sub_field;

import xyz.erupt.annotation.config.SerializeBy;
import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.annotation.sub_field.sub_edit.*;

import java.beans.Transient;

/**
 * Created by liyuepeng on 9/28/18.
 */
public @interface Edit {

    String title();

    String desc() default "";

    boolean notNull() default false;

    boolean show() default true;

    boolean readOnly() default false;

    String placeHolder() default "";

    Search search() default @Search(false);

    @Transient
    String orderBy() default "";

    @Transient
    Filter filter() default @Filter(condition = "");

    EditType type() default EditType.INPUT;

    @SerializeBy(method = "type", value = "INPUT")
    InputType inputType() default @InputType;

    @SerializeBy(method = "type", value = "BOOLEAN")
    BoolType boolType() default @BoolType(trueText = "是", falseText = "否");

    @SerializeBy(method = "type", value = "CHOICE")
    ChoiceType choiceType() default @ChoiceType(vl = {});

    @SerializeBy(method = "type", value = "DATE")
    DateType dateType() default @DateType;

    @SerializeBy(method = "type", value = "SLIDER")
    SliderType sliderType() default @SliderType(max = 9999);

    @SerializeBy(method = "type", value = "ATTACHMENT")
    AttachmentType attachmentType() default @AttachmentType;

    @SerializeBy(method = "type", value = "DEPEND_SWITCH")
    DependSwitchType dependSwitchType() default @DependSwitchType(dependSwitchAttrs = {});

    @SerializeBy(method = "type", value = "REFERENCE_TREE")
    ReferenceTreeType referenceTreeType() default @ReferenceTreeType;

    @SerializeBy(method = "type", value = "REFERENCE_TABLE")
    ReferenceTableType referenceTableType() default @ReferenceTableType;

}