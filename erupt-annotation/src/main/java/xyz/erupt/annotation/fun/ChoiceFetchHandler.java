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

    @Comment("下拉列表联动过滤，结果要保证包含fetch()的所有选项")
    default List<VLModel> fetchFilter(Map<String, Object> value, String[] params) {
        return this.fetch(params);
    }

}
