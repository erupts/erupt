package xyz.erupt.annotation.sub_field.sub_edit;

import java.beans.Transient;

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

    Type type() default Type.OTHER;

    int maxLimit() default 1;

    @Transient
    ImageType imageType() default @ImageType;

    SaveMode saveMode() default SaveMode.SINGLE_COLUMN;

    //当maxLimit大于1且SaveMode为SINGLE_COLUMN使用
    String fileSeparator() default ",";

    enum Type {
        IMAGE,
        OTHER,
    }

    @interface ImageType {
        //宽高使用长度为2的数组，第一位是最小宽高限制，第二位是最大宽高限制
        int[] width() default {};

        int[] height() default {};
    }

}