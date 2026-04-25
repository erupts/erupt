package xyz.erupt.annotation.cube;

public @interface Explore {

    String code();

    String name();

    String description() default "";

    String where() default "";

    ExploreParameter[] parameters() default {};

    Join[] joins() default {};

    boolean hidden() default false;

    String[] dimensions() default {};

    String[] measures() default {};

}
