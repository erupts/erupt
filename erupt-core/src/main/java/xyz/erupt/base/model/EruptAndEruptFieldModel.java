package xyz.erupt.base.model;

import lombok.Data;

/**
 * Created by liyuepeng on 2018-12-29.
 */
@Data
public class EruptAndEruptFieldModel {
    private EruptFieldModel eruptFieldModel;

    private EruptModel eruptModel;

    public EruptAndEruptFieldModel(EruptFieldModel eruptFieldModel, EruptModel eruptModel) {
        this.eruptFieldModel = eruptFieldModel;
        this.eruptModel = eruptModel;
    }


}
