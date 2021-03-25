package xyz.erupt.annotation.fun;

import xyz.erupt.annotation.config.Comment;

/**
 * @author YuePeng
 * date 2018-11-05.
 */
public interface FilterHandler {

    String filter(@Comment("过滤表达式") String condition, @Comment("注解参数") String[] params);

}
