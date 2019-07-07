package xyz.erupt.core.bean;

import lombok.Data;
import xyz.erupt.annotation.model.BoolAndReason;

/**
 * Created by liyuepeng on 10/9/18.
 */
@Data
public class EruptApiModel {

    private Status status;
    private PromptWay promptWay = PromptWay.DIALOG;

    public EruptApiModel(Status status, String message, Object data, PromptWay promptWay) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.promptWay = promptWay;
    }

    private String message;

    private Object data;

    private boolean errorIntercept = true;

    public EruptApiModel(Status status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public EruptApiModel(BoolAndReason boolAndReason) {
        if (boolAndReason.isBool()) {
            this.status = Status.SUCCESS;
        } else {
            this.status = Status.ERROR;
        }
        this.message = boolAndReason.getReason();
    }

    public static EruptApiModel successApi() {
        return new EruptApiModel(Status.SUCCESS, null, null, PromptWay.MESSAGE);
    }

    public static EruptApiModel successApi(Object data) {
        return new EruptApiModel(Status.SUCCESS, null, data, PromptWay.MESSAGE);
    }

    public static EruptApiModel successApi(String message, Object data) {
        return new EruptApiModel(Status.SUCCESS, null, data, PromptWay.MESSAGE);
    }

    public static EruptApiModel errorApi(String message) {
        return new EruptApiModel(Status.ERROR, message, null, PromptWay.DIALOG);
    }

    public static EruptApiModel errorNoInterceptApi(String message) {
        EruptApiModel eruptApiModel = new EruptApiModel(Status.ERROR, message, null);
        eruptApiModel.errorIntercept = false;
        return eruptApiModel;
    }

    public static EruptApiModel errorApi(Exception e) {
        e.printStackTrace();
        return new EruptApiModel(Status.ERROR, e.getMessage(), null, PromptWay.DIALOG);
    }

    public enum Status {
        INFO, SUCCESS, WARNING, ERROR,
    }

    public enum PromptWay {
        DIALOG, MESSAGE, NOTIFY
    }


}


