package xyz.erupt.annotation.sub_erupt;

import org.intellij.lang.annotations.Language;
import xyz.erupt.annotation.query.Direction;

public @interface Sort {

    @Language(value = "hql", prefix = "select * from t order by ")
    String field();

    Direction direction() default Direction.ASC;


}
