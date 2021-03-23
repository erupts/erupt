package xyz.erupt.core.view;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.fun.PowerObject;

import java.util.Map;

/**
 * @author YuePeng
 * date 2018-09-29.
 */
@Getter
@Setter
public class EruptBuildModel {

    private EruptModel eruptModel;

    private Map<String, EruptBuildModel> tabErupts;

    private Map<String, EruptModel> combineErupts;

    private Map<String, EruptModel> operationErupts;

    private PowerObject power;
}
