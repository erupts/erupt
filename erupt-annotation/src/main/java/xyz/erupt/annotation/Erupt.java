package xyz.erupt.annotation;

import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.config.Match;
import xyz.erupt.annotation.config.ToMap;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.sub_erupt.*;

import java.beans.Transient;
import java.lang.annotation.*;

/**
 * @author YuePeng
 * date 2018-09-28.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface Erupt {

    @Comment("主键列名")
    String primaryKeyCol() default "id";

    @Transient
    @Comment("功能名称")
    String name();

    @Comment("功能描述")
    String desc() default "";

    @Transient
    @Comment("访问该功能是否进行校验权限")
    boolean authVerify() default true;

    @Transient
    @Comment("操作权限配置")
    Power power() default @Power;

    @Comment("自定义功能按钮")
    RowOperation[] rowOperation() default {};

    @Comment("数据钻取")
    Drill[] drills() default {};

    @Transient
    @Comment("数据过滤表达式")
    Filter[] filter() default {};

    @Transient
    @Comment("排序表达式")
    String orderBy() default "";

    @Transient
    @Comment("数据行为代理接口，对增、删、改、查等行为做逻辑处理")
    Class<? extends DataProxy<?>>[] dataProxy() default {};

    @Comment("树视图配置")
    Tree tree() default @Tree;

    @Match("#value.field() != ''")
    @Comment("左树右表配置项")
    LinkTree linkTree() default @LinkTree(field = "");

    //布局配置
    Layout layout() default @Layout;

//    @Comment("自定义页头")
//    @Match("#value.path() != ''")
//    Tpl header() default @Tpl(path = "");
//
//    @Comment("画册视图定义")
//    @Match("#value.enable() == true")
//    Card cardView() default @Card(enable = false, galleryField = "", viewFields = {});

    @ToMap(key = "key")
    @Comment("自定义扩展参数")
    KV[] param() default {};

    @Deprecated
    @Transient
    Class<? extends Annotation> extra() default Annotation.class;

}
