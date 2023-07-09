package xyz.erupt.annotation.sub_erupt;

import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.fun.PowerHandler;

import java.beans.Transient;

/**
 * @author YuePeng
 * date 2018-09-28.
 */
public @interface Power {

    boolean add() default true;

    boolean edit() default true;

    boolean delete() default true;

    boolean query() default true;

    boolean viewDetails() default true;

    @Comment("导出")
    boolean export() default false;

    @Comment("导入")
    boolean importable() default false;

//    // 动态控制单行的编辑按钮是否展示
//    String editIfExpr() default "true";
//
//    // 动态控制单行删除按钮是否展示
//    String deleteIfExpr() default "true";
//
//    // 动态控制单行的查看详情按钮是否展示
//    String viewDetailIfExpr() default "true";

    @Transient
    @Comment("动态处理Power权限")
    Class<? extends PowerHandler> powerHandler() default PowerHandler.class;
}
