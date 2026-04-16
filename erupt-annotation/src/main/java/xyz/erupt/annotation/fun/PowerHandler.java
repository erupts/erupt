package xyz.erupt.annotation.fun;

import xyz.erupt.annotation.config.Comment;

/**
 * @author YuePeng
 * date 2019-11-25.
 */
public interface PowerHandler {

    @Comment("Dynamically control permissions for each feature")
    void handler(PowerObject power);


}
