package com.erupt.core.model;

/**
 * Created by liyuepeng on 10/9/18.
 */
public class EruptApiModel {

    private Status status;

    private Object data;

    enum Status {
        //200
        SUCCESS,
        //500
        ERROR,
        //
        NO_LOGIN,
        //401
        NO_RIGHT
    }
}
