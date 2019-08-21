package xyz.erupt.annotation.sub_field.sub_edit;

/**
 * Created by liyuepeng on 2019-01-13.
 */
public @interface DependSwitchType {
    Attr[] attr();

    boolean reject() default true;

    Type type() default Type.HIDDEN;

    enum Type {
        HIDDEN, DISABLE
    }

    @interface Attr {
        String value();

        String label();

        String[] dependEdits();
    }
}
