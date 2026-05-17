package xyz.erupt.annotation.fun;

import xyz.erupt.annotation.config.Comment;

import java.util.List;
import java.util.Map;

/**
 * @author YuePeng
 * date 2019-07-25.
 */
public interface ChoiceFetchHandler {

    @Comment("Fetch dropdown list")
    List<VLModel> fetch(String[] params);

    @Comment("Linked filtering for dropdown list; result must include all options from fetch()")
    default List<VLModel> fetchFilter(Map<String, Object> value, String[] params) {
        return this.fetch(params);
    }

}
