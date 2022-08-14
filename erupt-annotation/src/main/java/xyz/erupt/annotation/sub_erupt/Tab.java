package xyz.erupt.annotation.sub_erupt;

import xyz.erupt.annotation.config.Comment;

/**
 * 多页签
 *
 * @author YuePeng
 * date 2022/7/30 23:27
 */
@Deprecated
public @interface Tab {

    @Comment("页签名称")
    String name();

    @Comment("功能路径")
    String path();

}
