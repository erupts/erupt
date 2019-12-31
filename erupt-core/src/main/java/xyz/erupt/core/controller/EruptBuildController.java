package xyz.erupt.core.controller;


import org.springframework.web.bind.annotation.*;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.bean.EruptBuildModel;
import xyz.erupt.core.bean.EruptFieldModel;
import xyz.erupt.core.bean.EruptModel;
import xyz.erupt.core.constant.RestPath;
import xyz.erupt.core.service.CoreService;

import java.util.HashMap;

/**
 * Erupt 页面结构构建信息
 * @author liyuepeng
 * @date 2018-09-28.
 */
@RestController
@RequestMapping(RestPath.ERUPT_BUILD)
public class EruptBuildController {

    @GetMapping("/{erupt}")
    @ResponseBody
    @EruptRouter(authIndex = 1)
    public EruptBuildModel getEruptBuild(@PathVariable("erupt") String eruptName) {
        EruptModel eruptModel = CoreService.getErupt(eruptName);
        EruptBuildModel eruptBuildModel = new EruptBuildModel();
        eruptBuildModel.setEruptModel(eruptModel);
        for (EruptFieldModel fieldModel : eruptModel.getEruptFieldModels()) {
            switch (fieldModel.getEruptField().edit().type()) {
                case TAB_TREE:
                    if (eruptBuildModel.getTabErupts() == null) {
                        eruptBuildModel.setTabErupts(new HashMap<>(0));
                    }
                    EruptBuildModel eruptBuildModel1 = new EruptBuildModel();
                    eruptBuildModel1.setEruptModel(CoreService.getErupt(fieldModel.getFieldReturnName()));
                    eruptBuildModel.getTabErupts().put(fieldModel.getFieldName(), eruptBuildModel1);
                    break;
                case TAB_TABLE_ADD:
                case TAB_TABLE_REFER:
                    if (eruptBuildModel.getTabErupts() == null) {
                        eruptBuildModel.setTabErupts(new HashMap<>(0));
                    }
                    eruptBuildModel.getTabErupts().put(fieldModel.getFieldName(), getEruptBuild(fieldModel.getFieldReturnName()));
                    break;
                case COMBINE:
                    if (eruptBuildModel.getCombineErupts() == null) {
                        eruptBuildModel.setCombineErupts(new HashMap<>(0));
                    }
                    eruptBuildModel.getCombineErupts().put(fieldModel.getFieldName(), CoreService.getErupt(fieldModel.getFieldReturnName()));
                    break;
                case REFERENCE_TABLE:
                    if (eruptBuildModel.getReferenceErupts() == null) {
                        eruptBuildModel.setReferenceErupts(new HashMap<>(0));
                    }
                    eruptBuildModel.getReferenceErupts().put(fieldModel.getFieldName(), CoreService.getErupt(fieldModel.getFieldReturnName()));
                    break;
                default:
                    break;
            }
        }
        for (RowOperation operation : eruptBuildModel.getEruptModel().getErupt().rowOperation()) {
            if (operation.eruptClass() != void.class) {
                if (eruptBuildModel.getOperationErupts() == null) {
                    eruptBuildModel.setOperationErupts(new HashMap<>(0));
                }
                eruptBuildModel.getOperationErupts().put(operation.code(), CoreService.getErupt(operation.eruptClass().getSimpleName()));
            }
        }
        return eruptBuildModel;
    }

}
