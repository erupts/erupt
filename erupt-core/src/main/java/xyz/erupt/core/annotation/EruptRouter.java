package xyz.erupt.core.annotation;

import xyz.erupt.annotation.config.Comment;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.annotation.*;

/**
 * @author YuePeng
 * date 2019-06-11.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
@Comment("接口菜单权限校验，仅对/erupt-api/**下的接口有效")
public @interface EruptRouter {

    @Comment("根据请求地址'/'进行分隔，定义处于第几个下标为的字符为菜单权限标识字符")
    int authIndex() default 0;

    @Comment("配合authIndex使用，计算后权限下标位置为：skipAuthIndex + authIndex")
    int skipAuthIndex() default 2;

    @Comment("权限校验类型")
    VerifyType verifyType();

    @Comment("权限校验方式")
    VerifyMethod verifyMethod() default EruptRouter.VerifyMethod.HEADER;

    @Comment("定义路由校验规则")
    Class<? extends VerifyHandler> verifyHandler() default VerifyHandler.class;


    @Comment("自定义参数")
    String[] params() default {};

    enum VerifyMethod {
        @Comment("token必须放在请求头")
        HEADER,
        @Comment("token必须放在URL参数中")
        PARAM
    }

    enum VerifyType {
        @Comment("仅验证是否登录")
        LOGIN,
        @Comment("验证登录与菜单权限")
        MENU,
        @Comment("验证登录与erupt权限")
        ERUPT
    }

    interface VerifyHandler {

        /**
         * 动态转换授权菜单权限标识字符
         *
         * @param authStr 原始权限标识字符
         * @return 新的权限字符
         */
        default String convertAuthStr(EruptRouter eruptRouter, HttpServletRequest request, String authStr) {
            return authStr;
        }

    }

}