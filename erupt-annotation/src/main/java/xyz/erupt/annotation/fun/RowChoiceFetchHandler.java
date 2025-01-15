package xyz.erupt.annotation.fun;

import xyz.erupt.annotation.config.Comment;

import java.util.List;

/**
 * @author YuePeng
 * date 2019-07-25.
 */
public interface RowChoiceFetchHandler<T> {

    @Comment("根据行数据获取下拉列表")
    List<VLModel> fetch(T t, String[] params);

}
