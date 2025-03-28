package xyz.erupt.annotation.fun;

import xyz.erupt.annotation.config.Comment;

import java.util.List;
import java.util.Map;

/**
 * @author YuePeng
 * date 2019-07-25.
 */
public interface ChoiceFetchHandler {

    @Comment("获取下拉列表")
    List<VLModel> fetch(String[] params);

    default List<VLModel> fetchFilter(Map<String, Object> value, String[] params) {
        return fetch(params);
    }

}
