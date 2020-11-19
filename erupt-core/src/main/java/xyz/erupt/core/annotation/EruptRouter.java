package xyz.erupt.core.annotation;

import java.lang.annotation.*;

/**
 * @author liyuepeng
 * @date 2019-06-11.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface EruptRouter {

    int authIndex();

    int skipAuthIndex() default 2;

    VerifyType verifyType();

    //权限数据校验方式，请求头校验与参数校验可选
    VerifyMethod verifyMethod() default EruptRouter.VerifyMethod.HEADER;

    enum VerifyMethod {
        //token必须放在请求头
        HEADER,
        //token必须放在URL参数中
        PARAM
    }

    enum VerifyType {
        //仅验证登录
        LOGIN,
        //验证登录与erupt权限
        ERUPT,
        //验证登录与菜单权限
        MENU
    }

}
