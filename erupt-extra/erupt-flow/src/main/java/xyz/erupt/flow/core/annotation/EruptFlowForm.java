package xyz.erupt.flow.core.annotation;

import java.lang.annotation.*;

/**
 * 用在@Erupt的实体类上
 * 拥有此注解的类，可以被解析为表单，在流程中传递
 * 2023-05-07
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface EruptFlowForm {
}
