package xyz.erupt.cloud.server.base;

import lombok.Getter;
import lombok.Setter;

/**
 * @author YuePeng
 * date 2021/12/22 00:29
 */
@Getter
@Setter
public class R {

    private Object data;

    private Boolean success;

    private String message;

    public R(Boolean success, String message, Object data) {
        this.data = data;
        this.success = success;
        this.message = message;
    }

    public static R success() {
        return new R(true, null, null);
    }

    public static R success(String data) {
        return new R(true, null, data);
    }

    public static R error(String msg) {
        return new R(false, msg, null);
    }

}
