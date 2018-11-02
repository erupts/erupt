package com.erupt.model;

/**
 * Created by liyuepeng on 10/9/18.
 */
public class EruptApiModel {

    private Status status;

    private Object data;

    enum Status {
        SUCCESS, ERROR, NO_LOGIN, NO_RIGHT
    }
}
