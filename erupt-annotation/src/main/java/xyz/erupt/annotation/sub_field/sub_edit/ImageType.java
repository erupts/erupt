package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.constant.AnnotationConst;

/**
 * Created by liyuepeng on 2019-10-08.
 */
public @interface ImageType {
    //宽高使用长度为2的数组，第一位是最小宽高限制，第二位是最大宽高限制
    int minWidth() default 0;

    int maxWidth() default AnnotationConst.NOT_LIMIT;

    int minHeight() default 0;

    int maxHeight() default AnnotationConst.NOT_LIMIT;

    AttachmentType attachment() default @AttachmentType;

}
