package xyz.erupt.base.model;

import lombok.Data;

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

    public static EruptApiModel successApi(Object data) {
        return new EruptApiModel(true, null, data);
    }

    public static EruptApiModel errorApi(String message) {
        return new EruptApiModel(false, message, null);
    }


}
