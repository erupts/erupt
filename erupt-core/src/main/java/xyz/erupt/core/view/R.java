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

    //消息
    private String msg;

    //数据
    private T data;

    //是否成功
    private boolean success;

    //状态
    private EruptApiModel.Status status;

    //提示方式
    private EruptApiModel.PromptWay promptWay = EruptApiModel.PromptWay.MESSAGE;

    public static <T> R<T> ok(T data) {
        return new R<T>() {{
            this.setSuccess(true);
            this.setData(data);
            this.setStatus(EruptApiModel.Status.SUCCESS);
        }};
    }

    public static R<Void> error(String msg) {
        return new R<Void>() {{
            this.setSuccess(false);
            this.setMsg(msg);
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
