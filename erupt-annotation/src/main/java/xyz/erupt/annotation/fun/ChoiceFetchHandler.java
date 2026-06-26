package xyz.erupt.annotation.fun;

import xyz.erupt.annotation.config.Comment;

import java.util.List;

/**
 * @author YuePeng
 * date 2019-07-25.
 */
public interface ChoiceFetchHandler<MODEL> {

    @Comment("Fetch dropdown list")
    List<VLModel> fetch(String[] params);

    @Comment("Linked filtering for dropdown list; result must include all options from fetch()")
    default List<VLModel> fetchFilter(MODEL model, String[] params) {
        return this.fetch(params);
    }

}
