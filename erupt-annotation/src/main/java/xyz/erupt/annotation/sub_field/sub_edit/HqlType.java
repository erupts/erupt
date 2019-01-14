package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.LoadWay;

/**
 * Created by liyuepeng on 10/9/18.
 * 字段必须返回DictItem对象才能正常使用
 */
public @interface HqlType {
    String hql();

    LoadWay loadWay() default LoadWay.LAZY;

}
