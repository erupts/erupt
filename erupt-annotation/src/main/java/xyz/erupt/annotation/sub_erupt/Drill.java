package xyz.erupt.annotation.sub_erupt;

import java.beans.Transient;

/**
 * @author liyuepeng
 * @date 2020-02-17.
 */
public @interface Drill {

    String code();

    String title();

    String icon() default "";

    Class<?> eruptClass();

    String column() default "id";

    @Transient
    String joinColumn();
}
