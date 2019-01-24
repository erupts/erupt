package xyz.erupt.annotation.sub_field.sub_edit.sub_attachment;

/**
 * Created by liyuepeng on 2019-01-17.
 */
public @interface ImageType {
    /**
     * 宽高使用长度为2的数组，第一位是最小宽高限制，第二位是最大宽高限制
     *
     * @return
     */
    int[] width() default {};

    int[] height() default {};

}
