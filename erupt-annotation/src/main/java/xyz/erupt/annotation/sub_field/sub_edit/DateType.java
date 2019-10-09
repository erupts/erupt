package xyz.erupt.annotation.sub_field.sub_edit;

/**
 * Created by liyuepeng on 10/15/18.
 */
public @interface DateType {

    Type type() default Type.DATE;

    enum Type {
        DATE, DATE_TIME, MONTH, YEAR
    }
}
