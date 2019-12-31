package xyz.erupt.annotation.fun;

import com.google.gson.JsonObject;

/**
 * @author liyuepeng
 * @date 2018-10-09.
 */
public interface OperationHandler<T> {

    /**
     * 操作执行类
     *
     * @param data  待处理数据
     * @param param 处理参数
     */
    void exec(T data, JsonObject param);
}
