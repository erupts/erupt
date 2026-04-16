package xyz.erupt.annotation.fun;

import xyz.erupt.annotation.config.Comment;

import java.util.Map;

/**
 * @author YuePeng
 * date 2019-07-25.
 */
@Deprecated
public interface ChoiceTrigger {

    @Comment("Fetch dropdown list")
    @Comment("The return value will be filled into the form corresponding to the key")
    Map<String, Object> trigger(Object value, String[] params);

}
