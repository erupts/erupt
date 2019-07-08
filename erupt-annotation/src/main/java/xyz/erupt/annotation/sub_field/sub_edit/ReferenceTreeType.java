package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.constant.EruptConst;

import java.beans.Transient;

/**
 * Created by liyuepeng on 9/28/18.
 */
public @interface ReferenceTreeType {
    String id() default EruptConst.ID;

    String label() default EruptConst.LABEL;

    String pid() default "";

    String dependField() default "";

    @Transient
    String dependColumn() default EruptConst.ID;
}
