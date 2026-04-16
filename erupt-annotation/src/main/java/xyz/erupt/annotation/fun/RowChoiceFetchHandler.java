package xyz.erupt.annotation.fun;

import xyz.erupt.annotation.config.Comment;

import java.util.List;

/**
 * @author YuePeng
 * date 2019-07-25.
 */
public interface RowChoiceFetchHandler<T> {

    @Comment("Fetch dropdown list based on row data")
    List<VLModel> fetch(T t, String[] params);

}
