package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.sub_field.sub_edit.sub_attachment.AttachmentEnum;
import xyz.erupt.annotation.sub_field.sub_edit.sub_attachment.ImageType;

/**
 * Created by liyuepeng on 2019-01-23.
 */
public @interface AttachmentType {

    /**
     * 单位KB
     */
    long size() default 0;

    String[] fileTypes() default {};

    String path() default "";

    AttachmentEnum type() default AttachmentEnum.OTHER;

    ImageType imageType() default @ImageType;

    boolean muti() default false;

    /**
     * 与muti配合使用，muti为true时使用此属性来限制最大文件上传数
     */
    int maxLimit() default 5;

}
