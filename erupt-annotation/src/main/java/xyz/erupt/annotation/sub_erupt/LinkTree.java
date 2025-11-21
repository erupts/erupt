package xyz.erupt.annotation.sub_erupt;

import org.intellij.lang.annotations.Language;
import xyz.erupt.annotation.config.Comment;

public @interface LinkTree {

    @Comment("树字段")
    @Language(value = "java", prefix = "private String ", suffix = ";")
    String field();

    @Comment("表格的数据是否必须依赖树节点")
    boolean dependNode() default false;

}
