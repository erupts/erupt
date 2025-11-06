package xyz.erupt.annotation.viz;

import xyz.erupt.annotation.config.Comment;

/**
 * @author YuePeng
 * date 2025/10/30 23:52
 */
public @interface CardView {

    @Comment("Cover image effect")
    GalleryCover coverEffect() default GalleryCover.CLIP;

    @Comment("Cover image field")
    String coverField() default "";

    enum GalleryCover {
        FIT, //适应
        CLIP, //剪裁
    }

}
