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

    String path() default "";

    String[] fileTypes() default {};

    AttachmentEnum type() default AttachmentEnum.OTHER;

    ImageType imageType() default @ImageType;

    int maxLimit() default 1;

    SaveMode saveMode() default SaveMode.SINGLE_COLUMN;

    //当maxLimit大于1且SaveMode为SINGLE_COLUMN使用
    String fileSeparator() default ",";

}
