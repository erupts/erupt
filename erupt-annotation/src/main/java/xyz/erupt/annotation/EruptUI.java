package xyz.erupt.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface EruptUI {

    String addText() default "新增";

    String editText() default "修改";

    String deleteText() default "删除";

    String queryText() default "查询";

    String exportText() default "导出";

    String importText() default "导入";

}
