package xyz.erupt.bi.cube.annotation;

public @interface Explore {

    String name();

    String description() default "";

    String where() default "";

    Param[] params() default {};

    Join[] joins() default {};

    int cacheTime() default 0;

    @interface Param {

        String name();

        String value();

    }

}
