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

    @Comment("Whether erupt-cloud-server may forward this API to the node that owns the erupt. " +
            "Turn it off for a server-owned API that only keys on the erupt name (record comments, " +
            "for example): the node neither carries the module nor knows the server's users, so the " +
            "server must answer the call itself, under the 'nodeName.eruptName' it addressed")
    boolean cloudProxy() default true;

    @Comment("Rights of which the user needs at least one, besides the menu: checked by erupt-cloud-server " +
            "before it forwards a call to a node, so the node does not have to. Empty means the menu alone")
    Power[] power() default {};

    @Comment("What the request body carries, so erupt-cloud-server can validate it against the node's model " +
            "before forwarding")
    Payload payload() default Payload.NONE;

    @Comment("What erupt-cloud-server completes for a node's erupt: references a node sends as keys, a tree it does not build")
    Resolve resolve() default Resolve.NONE;

    @Comment("Custom parameters")
    String[] params() default {};

    // Mirrors the rights of PowerObject that a role can grant
    enum Power {
        QUERY, ADD, EDIT, DELETE, VIEW_DETAILS, EXPORT, IMPORTABLE
    }

    enum Resolve {
        NONE,
        @Comment("The response is a Page: references sent as keys get their label and flattened columns")
        PAGE,
        @Comment("The response is one record: references sent as keys get their label and flattened columns")
        RECORD,
        @Comment("A reference tree: built by the server from the referenced erupt's records when the node answers 404")
        REFERENCE_TREE,
        @Comment("The import template: written by the server from the build model when the node answers 404")
        EXCEL_TEMPLATE,
        @Comment("An export: the server pages through the node's table and encodes the file when the node answers 404")
        EXCEL_EXPORT,
        @Comment("An import: the server parses the file and adds its rows through the node when the node answers 404")
        EXCEL_IMPORT,
        @Comment("An upload: never forwarded, the server stores it by the rules of the node's build, so its download serves it")
        UPLOAD,
        @Comment("Choice / tags options: the server answers them from the node's build, forwarding only a field that fetches its own")
        OPTIONS,
        @Comment("A cell edit: the server reads the row through the node, patches the cell and saves the row through the node's update")
        CELL,
        @Comment("A batch edit: the cell edit of every selected row, done one row at a time through the node")
        CELLS
    }

    enum Payload {
        NONE,
        @Comment("TableQuery against the erupt in the path: search conditions, sort and paging")
        TABLE_QUERY,
        @Comment("One record of the erupt in the path, as added or updated")
        RECORD
    }

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