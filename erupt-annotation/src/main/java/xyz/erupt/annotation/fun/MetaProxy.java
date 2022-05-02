package xyz.erupt.annotation.fun;

import xyz.erupt.annotation.config.Comment;

/**
 * @author YuePeng
 * date 2018-10-09.
 */
public interface MetaProxy<MODEL> {

    @Comment("Don't call")
    default MetaProxy<? extends MODEL> dual() {
        return null;
    }

}
