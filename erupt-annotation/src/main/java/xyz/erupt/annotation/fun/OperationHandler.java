package xyz.erupt.annotation.fun;

import com.google.gson.JsonObject;
import xyz.erupt.annotation.model.BoolAndReason;

/**
 * Created by liyuepeng on 10/9/18.
 */
public interface OperationHandler {

    BoolAndReason exec(Object data, JsonObject param);
}
