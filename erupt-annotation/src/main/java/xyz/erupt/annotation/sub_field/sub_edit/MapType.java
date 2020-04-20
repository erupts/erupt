package xyz.erupt.annotation.sub_field.sub_edit;

/**
 * @author liyuepeng
 * @date 2020-02-15
 */
public @interface MapType {
    Mode value() default Mode.DEFAULT;

    enum Mode {
        DEFAULT,
        DRAW
    }
}
