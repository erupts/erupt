package xyz.erupt.core.constant;

/**
 * Created by liyuepeng on 12/10/18.
 */
public enum HttpStatus {
    NO_LOGIN(401),
    NO_RIGHT(403),
    NOT_FOUNT(404);

    public int code;

    HttpStatus(int code) {
        this.code = code;
    }
}
