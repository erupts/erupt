package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.config.Comment;

/**
 * @author YuePeng
 * date 2018-10-15.
 */
public @interface DateType {

    @Comment("Date/time type")
    Type type() default Type.DATE;

    @Comment("Picker mode")
    PickerMode pickerMode() default PickerMode.ALL;

    enum Type {
        DATE,
        TIME,
        DATE_TIME,
        MONTH,
        WEEK,
        YEAR
    }

    enum PickerMode {
        @Comment("Any time period can be selected")
        ALL,
        @Comment("Only future dates can be selected")
        FUTURE,
        @Comment("Only past dates can be selected")
        HISTORY
    }
}
