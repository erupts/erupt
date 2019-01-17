package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.LoadWay;

/**
 * Created by liyuepeng on 10/9/18.
 *
 */
public @interface HqlType {
    String hql();

    LoadWay loadWay() default LoadWay.LAZY;

}
