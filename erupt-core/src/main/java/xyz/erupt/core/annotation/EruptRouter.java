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

    int skipAuthIndex() default 1;

    VerifyType verifyType() default VerifyType.ERUPT;

    //权限数据传值方式，默认为放到请求头中
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
        //验证登录与erupt
        ERUPT,
        //验证登录与菜单权限
        MENU
    }

}
