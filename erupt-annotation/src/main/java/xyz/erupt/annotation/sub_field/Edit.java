package xyz.erupt.annotation.sub_field;

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

    InputType inputType() default @InputType;

    //如下注解虽为数组形式但是实际使用中只取数组为第零个的值(这样做可以避免大量的默认值生成，由此减轻前端json串体积)

    TextareaType[] textareaType() default {};

    ReferenceTreeType[] referenceTreeType() default @ReferenceTreeType;

    ReferenceTableType[] referenceTableType() default @ReferenceTableType;

//    CustomReferType[] customReferType() default {};

    BoolType boolType() default @BoolType(trueText = "是", falseText = "否", defaultValue = true);

    ChoiceType[] choiceType() default @ChoiceType(vl = {});

    DateType[] dateType() default @DateType;

    TabType[] tabType() default @TabType;

    SliderType[] sliderType() default @SliderType(max = 999);

    AttachmentType[] attachmentType() default @AttachmentType;

    DependSwitchType[] dependSwitchType() default @DependSwitchType(dependSwitchAttrs = {});

//    StepsType[] stepsTyps() default {};

}