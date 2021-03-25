package xyz.erupt.annotation.fun;

import xyz.erupt.annotation.config.Comment;

/**
 * @author YuePeng
 * date 2019-11-25.
 */
public interface PowerHandler {

    @Comment("动态控制各功能使用权限")
    void handler(PowerObject power);


}
