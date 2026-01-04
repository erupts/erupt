package xyz.erupt.annotation.vis;

import org.intellij.lang.annotations.Language;
import xyz.erupt.annotation.config.Comment;

/**
 * @author YuePeng
 * date 2025/10/30 23:52
 */
public @interface CardView {

    @Comment("Cover image effect")
    CoverEffect coverEffect() default CoverEffect.CLIP;

    @Comment("Cover image field")
    @Language(value = "hql", prefix = "select ", suffix = " from")
    String coverField() default "";

    enum CoverEffect {
        FIT, //适应
        CLIP, //剪裁
    }

}
