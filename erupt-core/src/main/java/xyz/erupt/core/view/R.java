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

    private String msg;

    private boolean success;

    private T data;

    public static R<Void> ok() {
        return ok(null);
    }

    public static <T> R<T> ok(T data) {
        return new R<T>() {{
            this.setSuccess(true);
            this.setMsg("success");
            this.setData(data);
        }};
    }

    public static R<Void> error(String msg) {
        return new R<Void>() {{
            this.setSuccess(false);
            this.setMsg(msg);
        }};
    }

}
