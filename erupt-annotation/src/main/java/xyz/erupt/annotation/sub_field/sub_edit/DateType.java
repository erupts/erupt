package xyz.erupt.annotation.sub_field.sub_edit;

/**
 * @author liyuepeng
 * @date 2018-10-15.
 */
public @interface DateType {

    Type type() default Type.DATE;

    PickerMode pickerMode() default PickerMode.ALL;

    enum Type {
        DATE, TIME, DATE_TIME, MONTH, WEEK, YEAR
    }

    /**
     * 选择器使用模式
     */
    enum PickerMode {
        ALL,
        FUTURE,  //仅可选择未来时间
        HISTORY  //仅可选择历史时间
    }
}
