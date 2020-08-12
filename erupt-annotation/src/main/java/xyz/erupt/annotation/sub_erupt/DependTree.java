package xyz.erupt.annotation.sub_erupt;

public @interface DependTree {

    String field();

    //表格的数据是否必须依赖Tree
    boolean relyNode() default true;

}
