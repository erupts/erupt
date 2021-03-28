package xyz.erupt.core.view;

import lombok.Getter;
import lombok.Setter;

/**
 * @author YuePeng
 * date 2018-10-09.
 */
@Getter
@Setter
public class EruptApiModel {

    private Status status;

    private PromptWay promptWay;

    private String message;

    private Object data;

    private boolean errorIntercept = true;

    public EruptApiModel(Status status, String message, Object data, PromptWay promptWay) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.promptWay = promptWay;
    }

    public EruptApiModel(Status status, String message, PromptWay promptWay) {
        this.status = status;
        this.message = message;
        this.promptWay = promptWay;
    }

    public static EruptApiModel successApi() {
        return new EruptApiModel(Status.SUCCESS, null, null, PromptWay.MESSAGE);
    }

    public static EruptApiModel successApi(String message, Object data) {
        return new EruptApiModel(Status.SUCCESS, message, data, PromptWay.MESSAGE);
    }

    public static EruptApiModel successApi(Object data) {
        return new EruptApiModel(Status.SUCCESS, null, data, PromptWay.MESSAGE);
    }

    public static EruptApiModel errorApi(String message) {
        return new EruptApiModel(Status.ERROR, message, null, PromptWay.DIALOG);
    }

    public static EruptApiModel errorNoInterceptApi(String message) {
        EruptApiModel eruptApiModel = new EruptApiModel(Status.ERROR, message, null, PromptWay.DIALOG);
        eruptApiModel.errorIntercept = false;
        return eruptApiModel;
    }

    public static EruptApiModel errorNoInterceptMessage(String message) {
        EruptApiModel eruptApiModel = new EruptApiModel(Status.ERROR, message, null, PromptWay.MESSAGE);
        eruptApiModel.errorIntercept = false;
        return eruptApiModel;
    }

    public static EruptApiModel errorApi(Exception e) {
        e.printStackTrace();
        return new EruptApiModel(Status.ERROR, e.getMessage(), null, PromptWay.DIALOG);
    }

    public enum Status {
        SUCCESS, ERROR, INFO, WARNING
    }

    public enum PromptWay {
        DIALOG, MESSAGE, NOTIFY
    }

}


