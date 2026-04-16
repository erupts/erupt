package xyz.erupt.annotation.sub_field.sub_edit;

import org.intellij.lang.annotations.Language;
import xyz.erupt.annotation.config.Comment;

import java.beans.Transient;

/**
 * @author YuePeng
 * date 2019-01-23.
 */
public @interface AttachmentType {

    @Comment("Attachment size limit in KB")
    long size() default -1;

    @Transient
    @Comment("Define a dedicated storage path for easier file lookup")
    @Language("file-reference")
    String path() default "";

    @Comment("Allowed file types for upload")
    String[] fileTypes() default {};

    @Comment("Attachment type")
    Type type() default Type.BASE;

    @Comment("Maximum number of uploads")
    int maxLimit() default 1;

    @Transient
    ImageType imageType() default @ImageType;

    @Comment("Separator character for multiple file paths")
    String fileSeparator() default "|";

    enum Type {
        @Comment("Any file type can be uploaded")
        BASE,
        @Comment("Image upload")
        IMAGE,
    }

    @interface ImageType {

        //Minimum width limit
        int minWidth() default 0;

        //Maximum width limit
        int maxWidth() default Integer.MAX_VALUE;

        //Minimum height limit
        int minHeight() default 0;

        //Maximum height limit
        int maxHeight() default Integer.MAX_VALUE;

    }

}