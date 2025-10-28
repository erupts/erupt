package xyz.erupt.annotation;

import xyz.erupt.annotation.sub_erupt.Filter;

public @interface ViewType {

    Filter[] filter() default {};

    String orderBy() default "";

    String title();

    View view() default View.TABLE;

    enum View {
        TABLE,
        GANTT,
        CARD,
        GALLERY,
    }

}
