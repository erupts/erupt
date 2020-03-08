package xyz.erupt.annotation.sub_erupt;

import java.beans.Transient;

/**
 * @author liyuepeng
 * @date 2020-03-08
 */
public @interface Link {
    Class<?> eruptClass();

    @Transient
    String column() default "id";

    @Transient
    String joinColumn();
}
