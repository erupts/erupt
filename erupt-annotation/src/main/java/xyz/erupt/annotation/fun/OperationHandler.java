package xyz.erupt.annotation.fun;

import com.google.gson.JsonObject;

/**
 * Created by liyuepeng on 10/9/18.
 */
public interface OperationHandler<T> {

    void exec(T data, JsonObject param);
}
