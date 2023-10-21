package xyz.erupt.core.view;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author YuePeng
 * date 2023/10/21 20:55
 */
@Getter
@Setter
public class EruptExceptionVo {

    private Date timestamp;

    private String path;

    private Integer status;

    private String error;

    private String message;

    public EruptExceptionVo(String path, Integer status, String error, String message) {
        this.path = path;
        this.status = status;
        this.message = message;
        this.error = error;
        this.timestamp = new Date();
    }
}
