package com.erupt.base.model;

/**
 * Created by liyuepeng on 10/9/18.
 */
public class EruptApiModel {

    private HttpStatus status;

    private String message;

    private Object data;

    public EruptApiModel(HttpStatus status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static EruptApiModel successApi(Object data) {
        return new EruptApiModel(HttpStatus.SUCCESS, null, data);
    }

    public static EruptApiModel errorApi(String message) {
        return new EruptApiModel(HttpStatus.ERROR, message, null);
    }


}
