package xyz.erupt.annotation.fun;

import com.google.gson.JsonObject;
import xyz.erupt.annotation.model.BoolAndReason;

import javax.transaction.Transactional;

/**
 * Created by liyuepeng on 10/9/18.
 */
@Transactional
public interface OperationHandler<T> {

    BoolAndReason exec(T data, JsonObject param);
}
