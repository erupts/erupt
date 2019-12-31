package xyz.erupt.core.annotation;

import xyz.erupt.annotation.constant.AnnotationConst;

import java.lang.annotation.*;

/**
 * @author liyuepeng
 * @date 2019-06-11.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
public @interface EruptRouter {
//    boolean base64() default false;

    boolean loginVerify() default true;

    boolean verifyErupt() default true;

    int authIndex() default AnnotationConst.NOT_LIMIT;

    int startAuthIndex() default 1;

    //权限数据传值方式，默认为放到请求头中
    VerifyMethod verifyMethod() default EruptRouter.VerifyMethod.HEADER;

    enum VerifyMethod {
        //校验内容必须放在请求头
        HEADER,
        //检验内容必须放到URL参数中
        PARAM
    }

}
