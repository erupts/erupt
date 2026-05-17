package xyz.erupt.annotation.fun;

import xyz.erupt.annotation.config.Comment;

/**
 * @author YuePeng
 * date 2018-11-05.
 */
public interface FilterHandler {

    String filter(@Comment("filter expression") String condition, @Comment("annotation parameters") String[] params);

}
