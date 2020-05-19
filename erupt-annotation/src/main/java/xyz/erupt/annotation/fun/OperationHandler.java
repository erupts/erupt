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
     * @param inputValue 处理参数
     * @param param RowOperation注解参数
     */
    void exec(T data, JsonObject inputValue, String[] param);
}
