package xyz.erupt.bi.cube.annotation;

public @interface Join {

    Class<?> cube();

    String type();

    String[] dimensions() default {};

    String[] measures() default {};

    String sqlOn() default "";  // ${cube1}.id = ${cube2}.id

}
