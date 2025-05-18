package xyz.erupt.annotation.sub_erupt;

public @interface Layout {

    //表单大小
    FormSize formSize() default FormSize.DEFAULT;

    //表格左侧固定列数量
    int tableLeftFixed() default 0;

    //表格右侧列固定数量
    int tableRightFixed() default 0;

    //分页方式
    PagingType pagingType() default PagingType.BACKEND;

    //分页大小
    int pageSize() default 10;

    //可选分页数
    int[] pageSizes() default {10, 20, 30, 50, 100, 300, 500};

    //数据更新时间，单位：毫秒
    int refreshTime() default -1;

    String tableWidth() default "";

    String tableOperatorWidth() default "";

    enum FormSize {
        //默认
        DEFAULT,
        //整行
        FULL_LINE
    }

    enum PagingType {
        //后端分页
        BACKEND,
        //前端分页
        FRONT,
        //不分页，最多显示：pageSizes[pageSizes.length - 1] * 10 条
        NONE
    }

}
