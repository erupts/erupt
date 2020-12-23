package xyz.erupt.annotation.sub_erupt;

import xyz.erupt.annotation.config.Comment;

/**
 * @author liyuepeng
 * @date 2020-02-17.
 */
public @interface Drill {

    String code();

    String title();

    @Comment("图标请参考Font Awesome")
    String icon() default "fa fa-sitemap";

    @Comment("下钻目标配置")
    Link link();
}
