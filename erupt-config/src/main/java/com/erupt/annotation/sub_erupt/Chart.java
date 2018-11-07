package com.erupt.annotation.sub_erupt;

/**
 * Created by liyuepeng on 11/6/18.
 */
public @interface Chart {
    //最大12，最小1。谢谢！
    int col() default 6;

    String type();

    String xAxis();

    String yAxis();

    String series();
}
