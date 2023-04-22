package xyz.erupt.annotation.sub_erupt;

import xyz.erupt.annotation.config.Comment;

import java.beans.Transient;

/**
 * 画册视图
 *
 * @author YuePeng
 * date 2022/10/9 23:52
 */
public @interface Card {

    @Transient
    boolean enable() default true;

    boolean showTitle() default false;

    @Comment("封面字段")
    String galleryField();

    @Comment("封面效果")
    GalleryCover galleryCover() default GalleryCover.CLIP;

    @Comment("显示字段")
    String[] viewFields();

    enum GalleryCover {
        FIT, //适应
        CLIP, //剪裁
    }

}
