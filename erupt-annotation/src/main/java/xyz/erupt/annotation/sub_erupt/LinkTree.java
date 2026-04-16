package xyz.erupt.annotation.sub_erupt;

import org.intellij.lang.annotations.Language;
import xyz.erupt.annotation.config.Comment;

public @interface LinkTree {

    @Comment("Tree field")
    @Language(value = "java", prefix = "private String ", suffix = ";")
    String field();

    @Comment("Whether the table data must depend on a tree node")
    boolean dependNode() default false;

}
