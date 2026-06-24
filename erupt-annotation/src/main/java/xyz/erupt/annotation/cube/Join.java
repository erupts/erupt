package xyz.erupt.annotation.cube;

import org.intellij.lang.annotations.Language;
import xyz.erupt.annotation.config.Comment;

public @interface Join {
    Class<?> cube();

    @Comment("Join type, e.g. left join")
    @Language(value = "sql", prefix = "select * from t ", suffix = " t1")
    String type() default "full join";

    @Comment("Join condition")
    @Language(value = "hql", prefix = "select * from t left join t1 on ")
    String on();  // ${cube1}.id = ${cube2}.id

    String[] dimensions() default {};

    String[] measures() default {};

}
