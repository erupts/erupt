package xyz.erupt.annotation.sub_erupt;

public @interface Layout {

    //表单大小
    FormSize formSize() default FormSize.DEFAULT;

    //表格左侧固定列数量
    int tableLeftFixed() default 0;

    //表格右侧列固定数量
    int tableRightFixed() default 0;


    enum FormSize {
        DEFAULT,  //默认
        FULL_LINE //整行
    }

}
