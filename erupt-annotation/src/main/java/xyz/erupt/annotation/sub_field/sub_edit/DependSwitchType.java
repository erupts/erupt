package xyz.erupt.annotation.sub_field.sub_edit;

/**
 * @author liyuepeng
 * @date 2019-01-13.
 */
public @interface DependSwitchType {
    Attr[] attr();

    boolean reject() default true;

    @interface Attr {
        String value();

        String label();

        String[] dependEdits();
    }
}
