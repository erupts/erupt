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

    private EruptModel eruptModel;

    private Map<String, EruptModel> tabErupts = new HashMap<>();

    private Map<String, EruptModel> referenceErupts = new HashMap<>();

    private Map<String, EruptModel> combineErupts = new HashMap<>();

    private List<EruptAndEruptFieldModel> subErupts = new ArrayList<>();
}
