package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.fun.CustomReferHandler;

/**
 * Created by liyuepeng on 2019-02-19.
 */
public @interface CustomReferType {


    String param() default "";

    Class<? extends CustomReferHandler> refer();

}
