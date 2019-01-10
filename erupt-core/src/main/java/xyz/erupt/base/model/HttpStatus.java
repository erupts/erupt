package xyz.erupt.base.model;

/**
 * Created by liyuepeng on 12/10/18.
 */
public enum HttpStatus {
    SUCCESS(200),
    //500
    ERROR(500),
    //403
    NO_LOGIN(401),
    //405
    NO_RIGHT(405),

    NOT_FOUNT(404);

    public int code;

    HttpStatus(int code) {
        this.code = code;
    }
}
