package xyz.erupt.annotation.sub_erupt;

import org.intellij.lang.annotations.Language;
import xyz.erupt.annotation.config.Comment;

import java.beans.Transient;

/**
 * @author YuePeng
 * date 2020-03-08
 */
public @interface Link {

    @Transient
    @Language(value = "java", prefix = "private String ", suffix = ";")
    String column() default "id";

    @Comment("erupt class to associate with. Note that this class needs to be configured with access permissions")
    Class<?> linkErupt();

    @Transient
    @Comment("Column in linkErupt → this.column = linkErupt.joinColumn")
    @Language(value = "hql", prefix = "select * from t where ",suffix = " = ''")
    String joinColumn();

    @Comment("Other conditions for the link")
    @Language(value = "hql", prefix = "select * from t where ")
    String linkCondition() default "";
}
