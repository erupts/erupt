package xyz.erupt.annotation.view;

import xyz.erupt.annotation.config.Comment;

/**
 * @author YuePeng
 * date 2022/10/9 23:52
 */
public @interface CardView {

    @Comment("封面图效果")
    GalleryCover galleryCover() default GalleryCover.CLIP;

    @Comment("封面图字段")
    String galleryField() default "";

    enum GalleryCover {
        FIT, //适应
        CLIP, //剪裁
    }

}
