package xyz.erupt.annotation;

import org.intellij.lang.annotations.Language;
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

    @Comment("Primary key column name")
    @Language(value = "java", prefix = "private String ", suffix = ";")
    String primaryKeyCol() default "id";

    @Transient
    @Comment("Feature name")
    String name();

    @Comment("Feature description")
    String desc() default "";

    @Transient
    @Comment("Whether to verify permissions when accessing this feature")
    boolean authVerify() default true;

    @Transient
    @Comment("Operation permission configuration")
    Power power() default @Power;

    @Comment("Custom operation buttons")
    RowOperation[] rowOperation() default {};

    @Comment("Data drill-down")
    Drill[] drills() default {};

    @Transient
    @Comment("Data filter expression")
    Filter[] filter() default {};

    @Transient
    @Comment("Sort expression")
    @Language(value = "hql", prefix = "select * from t order by ")
    String orderBy() default "";

    @Transient
    @Comment("Data behavior proxy interface; handles logic for add, delete, update, query, and other operations")
    Class<? extends DataProxy<?>>[] dataProxy() default {};

    @Comment("This value can be retrieved via DataProxyContext.get() inside dataProxy")
    String[] dataProxyParams() default {};

    @Comment("Tree view configuration")
    Tree tree() default @Tree;

    @Match("#value.field() != ''")
    @Comment("Left-tree right-table configuration")
    LinkTree linkTree() default @LinkTree(field = "");

    Layout layout() default @Layout;

    boolean visRawTable() default true;

    Vis[] vis() default {};

    @ToMap(key = "key")
    @Comment("Custom extension parameters")
    KV[] param() default {};

    Class<? extends Annotation>[] extra() default {};

}
