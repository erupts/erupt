package xyz.erupt.annotation.sub_erupt;

public @interface Layout {

    //表单大小
    FormSize formSize() default FormSize.DEFAULT;

    //表格左侧固定列数量
    int tableLeftFixed() default 0;

    //表格右侧列固定数量
    int tableRightFixed() default 0;

    //分页大小
    int pageSize() default 10;

    //可选分页数
    int[] pageSizes() default {10, 20, 30, 50, 100, 300, 500};


    enum FormSize {
        DEFAULT,  //默认
        FULL_LINE //整行
    }

}
