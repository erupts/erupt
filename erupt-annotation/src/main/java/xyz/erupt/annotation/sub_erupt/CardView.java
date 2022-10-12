package xyz.erupt.annotation.sub_erupt;

import xyz.erupt.annotation.config.Comment;

/**
 * 画册视图
 *
 * @author YuePeng
 * date 2022/10/9 23:52
 */
public @interface CardView {

    boolean enable() default true;

    @Comment("封面字段")
    String galleryField() default "";

    @Comment("封面效果")
    GalleryCover galleryCover() default GalleryCover.FIT;


    @Comment("字段")
    String[] viewFields();

    enum GalleryCover {
        FIT, //适应
        CLIP, //剪裁
    }

}
