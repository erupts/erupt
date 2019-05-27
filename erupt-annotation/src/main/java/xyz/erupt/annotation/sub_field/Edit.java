package xyz.erupt.annotation.sub_field;

import xyz.erupt.annotation.SerializeBy;
import xyz.erupt.annotation.sub_field.sub_edit.*;

/**
 * Created by liyuepeng on 9/28/18.
 */
public @interface Edit {

    String title();

    String desc() default "";

    boolean notNull() default false;

    boolean show() default true;

    boolean readOnly() default false;

    Search search() default @Search(false);

    EditType type() default EditType.INPUT;

    @SerializeBy(method = "type", value = "INPUT")
    InputType inputType() default @InputType;

    //如下注解虽为数组形式但是实际使用中只取数组为第零个的值(这样做可以避免大量的默认值生成，由此减轻前端json串体积)

    @SerializeBy(method = "type", value = "REFERENCE_TREE")
    ReferenceTreeType[] referenceTreeType() default @ReferenceTreeType;

    @SerializeBy(method = "type", value = "REFERENCE_TABLE")
    ReferenceTableType[] referenceTableType() default @ReferenceTableType;

//    CustomReferType[] customReferType() default {};

    @SerializeBy(method = "type", value = "BOOLEAN")
    BoolType[] boolType() default {};

    @SerializeBy(method = "type", value = "CHOICE")
    ChoiceType[] choiceType() default @ChoiceType(vl = {});

    @SerializeBy(method = "type", value = "DATE")
    DateType[] dateType() default @DateType;

    @SerializeBy(method = "type", value = "TAB")
    TabType[] tabType() default @TabType;

    @SerializeBy(method = "type", value = "SLIDER")
    SliderType[] sliderType() default @SliderType(max = 999);

    @SerializeBy(method = "type", value = "ATTACHMENT")
    AttachmentType[] attachmentType() default @AttachmentType;

    @SerializeBy(method = "type", value = "DEPEND_SWITCH")
    DependSwitchType[] dependSwitchType() default @DependSwitchType(dependSwitchAttrs = {});

//    StepsType[] stepsTyps() default {};

}