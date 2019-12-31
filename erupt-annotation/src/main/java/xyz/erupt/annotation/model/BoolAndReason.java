package xyz.erupt.annotation.model;

/**
 * 用来描述bool值所产生的的原因
 * @author liyuepeng
 * @date 2018-11-20.
 */
public class BoolAndReason {
    private boolean bool;

    private String reason;

    public BoolAndReason(boolean bool, String reason) {
        this.bool = bool;
        this.reason = reason;
    }

    public boolean isBool() {
        return bool;
    }

    public void setBool(boolean bool) {
        this.bool = bool;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
