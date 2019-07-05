package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.EruptConst;

/**
 * Created by liyuepeng on 9/28/18.
 */
public @interface ReferenceTableType {
    String id() default EruptConst.ID;

    String label() default EruptConst.LABEL;
}
