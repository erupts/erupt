package xyz.erupt.core.model;

import lombok.Data;

import java.util.List;

/**
 * Created by liyuepeng on 9/29/18.
 */
@Data
public class EruptBuildModel {

    EruptModel eruptModel;

//    List<EruptAndEruptFieldModel> referenceErupt;

    List<EruptAndEruptFieldModel> combineErupts;

    List<EruptAndEruptFieldModel> subErupts;
}
