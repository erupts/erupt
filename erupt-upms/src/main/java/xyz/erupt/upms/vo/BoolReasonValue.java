package xyz.erupt.upms.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * @author liyuepeng
 * @date 2021/2/13 21:55
 */
@Getter
@Setter
public class BoolReasonValue<T> {

    private boolean bool;

    private String reason;

    private T value;

    public BoolReasonValue(boolean bool, String reason) {
        this.bool = bool;
        this.reason = reason;
    }

    public BoolReasonValue(boolean bool, String reason, T value) {
        this.bool = bool;
        this.reason = reason;
        this.value = value;
    }

    public BoolReasonValue() {
    }
}
