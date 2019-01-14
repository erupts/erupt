package xyz.erupt.core.model;

import lombok.Data;
import xyz.erupt.annotation.model.BoolAndReason;

/**
 * Created by liyuepeng on 10/9/18.
 */
@Data
public class EruptApiModel {

    private boolean success;

    private String message;

    private Object data;

    public EruptApiModel(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public EruptApiModel(BoolAndReason boolAndReason) {
        this.success = boolAndReason.isBool();
        this.message = boolAndReason.getReason();
    }

    public static EruptApiModel successApi(Object data) {
        return new EruptApiModel(true, null, data);
    }

    public static EruptApiModel errorApi(String message) {
        return new EruptApiModel(false, message, null);
    }


}
