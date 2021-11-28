package xyz.erupt.annotation.fun;

import xyz.erupt.annotation.config.Comment;

/**
 * @author YuePeng
 * date 2018-10-09.
 */
public interface IEruptProxy {

    @Comment("Don't call")
    default void dual() {
    }

}
