package xyz.erupt.annotation.sub_erupt;

import xyz.erupt.annotation.constant.AnnotationConst;

/**
 * @author liyuepeng
 * @date 2019-11-13.
 */
public @interface Tree {

    String id() default AnnotationConst.ID;

    String label() default AnnotationConst.LABEL;

    String pid() default "";

    Link[] linkTable() default {};

}
