package xyz.erupt.annotation.sub_field;

import java.beans.Transient;

public @interface Drill {

    boolean enable() default true;

    Class<?> eruptClass();

    @Transient
    String joinColumn();
}
