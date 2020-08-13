package xyz.erupt.annotation.sub_erupt;

public @interface LinkTree {

    String field();

    //表格的数据是否必须依赖树节点
    boolean dependNode() default true;

}
