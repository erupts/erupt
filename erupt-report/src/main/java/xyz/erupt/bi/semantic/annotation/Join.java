package xyz.erupt.bi.semantic.annotation;

public @interface Join {

    String name()        default "";      // explore 里 join 的别名

    String cube()        default "";      // 被 join 的 view 名

    String sqlOn()       default "";      // ${view1}.id = ${view2}.id

    String type()        default "left_outer"; // left_outer, inner, full

}
