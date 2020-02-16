package xyz.erupt.annotation.sub_erupt;

import java.beans.Transient;

public @interface Drill {

    String code();

    String title();

    String icon() default "";

    Class<?> eruptClass();

    @Transient
    String joinColumn();
}
