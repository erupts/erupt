package xyz.erupt.core.exception;

import xyz.erupt.core.view.EruptApiModel;

/**
 * @author liyuepeng
 * @date 2020-04-10
 */
public class EruptApiErrorTip extends RuntimeException {

    public EruptApiModel eruptApiModel;

    public EruptApiErrorTip(String message) {
        eruptApiModel = EruptApiModel.errorApi(message);
    }

    public EruptApiErrorTip(EruptApiModel eruptApiModel) {
        this.eruptApiModel = eruptApiModel;
    }

}
