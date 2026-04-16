package xyz.erupt.core.annotation;

import jakarta.servlet.http.HttpServletRequest;
import xyz.erupt.annotation.config.Comment;

import java.lang.annotation.*;

/**
 * @author YuePeng
 * date 2019-06-11.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
@Comment("API menu permission verification, only effective for interfaces under /erupt-api/**")
public @interface EruptRouter {

    @Comment("Split the request path by '/', defines which index position character serves as the menu permission identifier")
    int authIndex() default 0;

    @Comment("Used together with authIndex; the effective permission index position is: skipAuthIndex + authIndex")
    int skipAuthIndex() default 2;

    @Comment("High-privilege security check mechanism; must not be bypassed for any reason")
    boolean highSafe() default false;

    @Comment("Permission verification type")
    VerifyType verifyType();

    @Comment("Permission verification method")
    VerifyMethod verifyMethod() default EruptRouter.VerifyMethod.HEADER;

    @Comment("Define route verification rules")
    Class<? extends VerifyHandler> verifyHandler() default VerifyHandler.class;


    @Comment("Custom parameters")
    String[] params() default {};

    enum VerifyMethod {
        @Comment("Token must be placed in the request header")
        HEADER,
        @Comment("Token must be placed in the URL parameter")
        PARAM
    }

    enum VerifyType {
        @Comment("Verify login status only")
        LOGIN,
        @Comment("Verify login and menu permissions")
        MENU,
        @Comment("Verify login and erupt permissions")
        ERUPT
    }

    interface VerifyHandler {

        /**
         * Dynamically convert the authorization menu permission identifier string
         *
         * @param authStr the original permission identifier string
         * @return the new permission string
         */
        default String convertAuthStr(EruptRouter eruptRouter, HttpServletRequest request, String authStr) {
            return authStr;
        }

    }

}