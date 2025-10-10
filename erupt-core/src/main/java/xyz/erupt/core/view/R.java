package xyz.erupt.core.view;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author YuePeng
 */
@Getter
@Setter
public class R<T> implements Serializable {

    //数据
    private T data;

    //消息
    private String message;

    //是否成功
    private boolean success;

    //状态
    private EruptApiModel.Status status;

    //提示方式
    private EruptApiModel.PromptWay promptWay = EruptApiModel.PromptWay.MESSAGE;

    public static <T> R<T> ok(T data) {
        R<T> r = new R<>();
        r.setSuccess(true);
        r.setData(data);
        r.setPromptWay(EruptApiModel.PromptWay.NONE);
        r.setStatus(EruptApiModel.Status.SUCCESS);
        return r;
    }

    public static <T> R<T> ok() {
        R<T> r = new R<>();
        r.setSuccess(true);
        r.setStatus(EruptApiModel.Status.SUCCESS);
        return r;
    }

    public static <T> R<T> error(String message) {
        R<T> r = new R<>();
        r.setSuccess(false);
        r.setMessage(message);
        r.setStatus(EruptApiModel.Status.ERROR);
        return r;
    }

    public static <T> R<T> errorNotIntercept(String message, T data) {
        R<T> r = new R<>();
        r.setSuccess(false);
        r.setMessage(message);
        r.setPromptWay(EruptApiModel.PromptWay.NONE);
        r.setStatus(EruptApiModel.Status.ERROR);
        r.setData(data);
        return r;
    }

    public static <T> R<T> errorNotIntercept(T data) {
        return errorNotIntercept(null, data);
    }

    public enum Status {
        SUCCESS, ERROR, INFO, WARNING
    }

    public enum PromptWay {
        DIALOG, MESSAGE, NOTIFY, NONE
    }

}
