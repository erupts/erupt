package xyz.erupt.core.exception;

import xyz.erupt.core.view.R;

/**
 * @author YuePeng
 * date 2020-04-10
 */
public class EruptApiErrorTip extends RuntimeException {

    public R<?> r;

    public EruptApiErrorTip(String message) {
        super(message);
        r = R.errorDialog(message);
    }

    public EruptApiErrorTip(String message, R.PromptWay promptWay) {
        super(message);
        R<Void> rr = new R<>();
        rr.setSuccess(false);
        rr.setMessage(message);
        rr.setStatus(R.Status.ERROR);
        rr.setPromptWay(promptWay);
        this.r = rr;
    }

    public EruptApiErrorTip(R.Status status, String message, R.PromptWay promptWay) {
        super(message);
        R<Void> rr = new R<>();
        rr.setSuccess(status == R.Status.SUCCESS);
        rr.setMessage(message);
        rr.setStatus(status);
        rr.setPromptWay(promptWay);
        this.r = rr;
    }

    public EruptApiErrorTip(R<?> r) {
        super(r.getMessage());
        this.r = r;
    }

}
