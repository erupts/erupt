package xyz.erupt.core.annotation;

import java.lang.annotation.*;

/**
 * Created by liyuepeng on 2019-06-11.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
public @interface EruptRouter {
    boolean base64() default false;

    boolean loginVerify() default true;

    int verifyIndex() default -1;

    //权限数据传值方式，默认为放到请求头中
    VerifyMethod verifyMethod() default EruptRouter.VerifyMethod.HEADER;

    enum VerifyMethod {
        HEADER, PARAM
    }

}
