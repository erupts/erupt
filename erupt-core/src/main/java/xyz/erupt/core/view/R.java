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

    private boolean errorIntercept = true;

    public static <T> R<T> ok(T data) {
        return new R<T>() {{
            this.setSuccess(true);
            this.setData(data);
            this.setPromptWay(EruptApiModel.PromptWay.NONE);
            this.setStatus(EruptApiModel.Status.SUCCESS);
        }};
    }


    public static <T> R<T> ok() {
        return new R<T>() {{
            this.setSuccess(true);
            this.setStatus(EruptApiModel.Status.SUCCESS);
        }};
    }

    public static <T> R<T> error(String message) {
        return new R<T>() {{
            this.setSuccess(false);
            this.setMessage(message);
            this.setStatus(EruptApiModel.Status.ERROR);
        }};
    }

    public enum Status {
        SUCCESS, ERROR, INFO, WARNING
    }

    public enum PromptWay {
        DIALOG, MESSAGE, NOTIFY, NONE
    }

}
