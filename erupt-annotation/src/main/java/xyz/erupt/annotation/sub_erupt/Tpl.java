package xyz.erupt.annotation.sub_erupt;

import lombok.Getter;
import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.constant.PageEmbedType;

import java.beans.Transient;
import java.util.Map;

/**
 * @author YuePeng
 * date 2019-10-16.
 */
public @interface Tpl {

    boolean enable() default true;

    @Comment("Template file path or route address")
    String path();

    @Transient
    @Comment("The defined values can be retrieved in tplHandler")
    String[] params() default {};

    @Transient
    @Comment("Define template binding data")
    Class<? extends TplHandler> tplHandler() default TplHandler.class;

    @Transient
    @Comment("Template engine")
    Engine engine() default Engine.FreeMarker;

    @Comment("Page embed mode: Iframe or micro-frontend")
    PageEmbedType embedType() default PageEmbedType.IFRAME;

    @Comment("Popup layer width")
    String width() default "";

    @Comment("Popup layer height")
    String height() default "";

    @Comment("Popup layer open mode")
    OpenWay openWay() default OpenWay.MODAL;

    @Comment("Drawer open direction")
    Placement drawerPlacement() default Placement.RIGHT;

    @Getter
    enum Engine {
        @Comment("Native H5; tplHandler is not supported in Native mode")
        Native,
        @Comment("FreeMarker")
        FreeMarker,
        @Comment("Thymeleaf")
        Thymeleaf,
        @Comment("Velocity")
        Velocity,
        @Comment("Beetl")
        Beetl,
        @Comment(("JFinal Enjoy"))
        Enjoy
    }

    interface TplHandler {

        /**
         * 1.7.0 And above use the Function
         */
        void bindTplData(Map<String, Object> binding, String[] params);

    }

}
