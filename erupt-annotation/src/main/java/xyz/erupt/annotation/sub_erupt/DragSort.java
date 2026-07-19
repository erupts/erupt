package xyz.erupt.annotation.sub_erupt;

import org.intellij.lang.annotations.Language;
import xyz.erupt.annotation.config.Comment;

/**
 * @author YuePeng
 * date 2026-07-18.
 */
public @interface DragSort {

    @Comment("Numeric field that stores the row order value; drag sort is enabled when not empty")
    @Language(value = "java", prefix = "private Integer ", suffix = ";")
    String field();

}
