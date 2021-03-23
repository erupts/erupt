package xyz.erupt.annotation.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;

/**
 * @author YuePeng
 * date 2020-12-23.
 */
@Repeatable(Comments.class)
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.FIELD, ElementType.TYPE_PARAMETER, ElementType.PARAMETER})
@Documented
public @interface Comment {

    String value();

    Language language() default Language.ZH;

    enum Language {
        ZH, EN
    }

}