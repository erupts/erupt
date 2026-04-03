package xyz.erupt.annotation.cube;

import org.intellij.lang.annotations.Language;

public @interface Join {

//    String as() default "";

    Class<?> cube();

    @Language(value = "sql", prefix = "select * from t ", suffix = " t1")
    String type() default "full join";

    @Language(value = "hql", prefix = "select * from t left join t1 on ")
    String on();  // ${cube1}.id = ${cube2}.id

    String[] dimensions() default {};

    String[] measures() default {};

}
