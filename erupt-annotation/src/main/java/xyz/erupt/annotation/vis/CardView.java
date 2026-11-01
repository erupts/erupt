package xyz.erupt.annotation.vis;

import org.intellij.lang.annotations.Language;
import xyz.erupt.annotation.config.Comment;

/**
 * @author YuePeng
 * date 2025/10/30 23:52
 */
public @interface CardView {

    @Comment("Cover image effect; ignored by MASONRY, which shows the whole cover at its own height")
    CoverEffect coverEffect() default CoverEffect.CLIP;

    @Comment("Cover image field")
    @Language(value = "hql", prefix = "select ", suffix = " from")
    String coverField() default "";

    @Comment("GRID lines up equal-size cards in rows; MASONRY keeps each cover's own height and packs cards into columns")
    Layout layout() default Layout.GRID;

    enum Layout {
        GRID,
        MASONRY
    }

    enum CoverEffect {
        FIT, //Fit
        CLIP, //Clip
    }

}
