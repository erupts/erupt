package com.erupt.model;

/**
 * Created by liyuepeng on 10/9/18.
 */
public class EruptApiModel {

    private Status status;

    private Object data;

    public enum Status {
        //200
        SUCCESS(200),
        //500
        ERROR(500),
        //403
        NO_LOGIN(403),
        //405
        NO_RIGHT(405);

        public int code;

        Status(int code) {
            this.code = code;
        }
    }
}
