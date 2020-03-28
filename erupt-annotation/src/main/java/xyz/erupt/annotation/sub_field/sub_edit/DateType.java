package xyz.erupt.annotation.sub_field.sub_edit;

/**
 * @author liyuepeng
 * @date 2018-10-15.
 */
public @interface DateType {

    Type type() default Type.DATE;

    boolean limit() default true;

    enum Type {
        DATE, TIME, DATE_TIME, MONTH, YEAR
    }
}
