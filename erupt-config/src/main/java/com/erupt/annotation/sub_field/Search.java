package com.erupt.annotation.sub_field;

/**
 * Created by liyuepeng on 9/28/18.
 */
public @interface Search {
    boolean isSearch();

    boolean isFuzzy() default false;

    boolean range() default false;
}
