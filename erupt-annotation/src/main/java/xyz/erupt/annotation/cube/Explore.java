package xyz.erupt.annotation.cube;

public @interface Explore {

    // default explore code, represented by the bare cube name instead of a separate "Cube.overview" table
    String OVERVIEW = "overview";

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
