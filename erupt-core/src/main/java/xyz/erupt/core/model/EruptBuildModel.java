package xyz.erupt.core.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liyuepeng on 9/29/18.
 */
@Data
public class EruptBuildModel {

    EruptModel eruptModel;

    Map<String, EruptModel> referenceErupts = new HashMap<>();

    Map<String, EruptModel> combineErupts = new HashMap<>();

    List<EruptAndEruptFieldModel> subErupts = new ArrayList<>();
}
