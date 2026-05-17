package xyz.erupt.annotation.sub_erupt;

public @interface Layout {

    //Form size
    FormSize formSize() default FormSize.DEFAULT;

    //Number of fixed columns on the left side of the table
    int tableLeftFixed() default 0;

    //Number of fixed columns on the right side of the table
    int tableRightFixed() default 0;

    //Paging mode
    PagingType pagingType() default PagingType.BACKEND;

    //Page size
    int pageSize() default 10;

    //Available page size options
    int[] pageSizes() default {10, 20, 30, 50, 100, 300, 500};

    //Data refresh interval in milliseconds
    int refreshTime() default -1;

    String tableWidth() default "";

    String tableOperatorWidth() default "";

    enum FormSize {
        //Default
        DEFAULT,
        //Full line
        FULL_LINE
    }

    enum PagingType {
        //Backend paging
        BACKEND,
        //Frontend paging
        FRONT,
        //No paging; maximum display: pageSizes[pageSizes.length - 1] * 10 records
        NONE
    }

}
