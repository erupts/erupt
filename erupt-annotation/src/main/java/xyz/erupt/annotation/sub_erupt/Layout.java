package xyz.erupt.annotation.sub_erupt;

public @interface Layout {

    //表单大小
    FormSize formSize() default FormSize.DEFAULT;

    //表格宽度，为0则自动计算
    int tableWidth() default 0;

    int tableLeftFixed() default 0;

    int tableRightFixed() default 0;


    enum FormSize {
        DEFAULT,
        FULL_LINE //整行
    }

}
