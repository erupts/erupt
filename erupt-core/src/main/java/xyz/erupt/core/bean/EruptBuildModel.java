package xyz.erupt.core.bean;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * Created by liyuepeng on 9/29/18.
 */
@Getter
@Setter
public class EruptBuildModel {

    private EruptModel eruptModel;

    private Map<String, EruptBuildModel> tabErupts;

    private Map<String, EruptModel> referenceErupts;

    private Map<String, EruptModel> combineErupts;

    private Map<String, EruptModel> operationErupts;
}
