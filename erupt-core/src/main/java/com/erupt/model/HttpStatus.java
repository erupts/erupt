package com.erupt.model;

/**
 * Created by liyuepeng on 12/10/18.
 */
public enum HttpStatus {
    SUCCESS(200),
    //500
    ERROR(500),
    //403
    NO_LOGIN(403),
    //405
    NO_RIGHT(405);

    public int code;

    HttpStatus(int code) {
        this.code = code;
    }
}
