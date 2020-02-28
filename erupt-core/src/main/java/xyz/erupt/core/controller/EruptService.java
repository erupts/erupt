package xyz.erupt.core.controller;

import org.springframework.stereotype.Service;
import xyz.erupt.core.util.AnnotationUtil;
import xyz.erupt.core.view.EruptModel;

/**
 * @author liyuepeng
 * @date 2020-02-29
 */
@Service
public class EruptService {


    public void findEruptData(EruptModel eruptModel, String id) {
        //filter condition
        String condition = AnnotationUtil.switchFilterConditionToStr(eruptModel.getErupt().filter());

    }
}
