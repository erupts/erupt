package com.erupt.annotation.model;

/**
 * Created by liyuepeng on 11/20/18.
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
