package xyz.erupt.core.exception;

import xyz.erupt.core.view.EruptApiModel;

/**
 * @author YuePeng
 * date 2020-04-10
 */
public class EruptApiErrorTip extends RuntimeException {

    public EruptApiModel eruptApiModel;

    public EruptApiErrorTip(String message) {
        super(message);
        eruptApiModel = EruptApiModel.errorApi(message);
    }

    public EruptApiErrorTip(String message, EruptApiModel.PromptWay promptWay) {
        super(message);
        this.eruptApiModel = new EruptApiModel(EruptApiModel.Status.ERROR, message, promptWay);
    }

    public EruptApiErrorTip(EruptApiModel.Status status, String message, EruptApiModel.PromptWay promptWay) {
        super(message);
        this.eruptApiModel = new EruptApiModel(status, message, promptWay);
    }

    public EruptApiErrorTip(EruptApiModel eruptApiModel) {
        super(eruptApiModel.getMessage());
        this.eruptApiModel = eruptApiModel;
    }

}
