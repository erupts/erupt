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

    Search search() default @Search;

    EditType type() default EditType.INPUT;

    //如下注解虽为数组形式但是实际使用中只取数组为第零个的值(这样做可以避免大量的默认值生成，由此减轻前端json串体积)
    InputType[] inputType() default @InputType;

    ReferenceType[] referenceType() default {};

    BoolType[] boolType() default {};

    ChoiceType[] choiceType() default {};

    HqlType[] dictType() default {};

    DateType[] dateType() default {};

    TabType[] tabType() default {};

    SliderType[] sliderType() default {};

    ImageType[] imageType() default {};

    DependSwitchType[] dependSwitchType() default {};

}
