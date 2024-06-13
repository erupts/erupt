package xyz.erupt.annotation.fun;

import xyz.erupt.annotation.config.Comment;

import java.util.Map;

/**
 * @author YuePeng
 * date 2019-07-25.
 */
public interface ChoiceTrigger {

    @Comment("获取下拉列表")
    @Comment("返回值会填充给key对应表单中")
    Map<String, Object> trigger(Object value, String[] params);

}
