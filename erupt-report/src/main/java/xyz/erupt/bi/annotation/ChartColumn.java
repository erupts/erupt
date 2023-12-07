package xyz.erupt.bi.annotation;

import java.lang.annotation.*;

/**
 * @author YuePeng
 * date 2023/12/6 00:02
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface ChartColumn {

    String[] value();

}
