package xyz.erupt.core.controller;


import org.springframework.web.bind.annotation.*;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.bean.EruptBuildModel;
import xyz.erupt.core.bean.EruptFieldModel;
import xyz.erupt.core.bean.EruptModel;
import xyz.erupt.core.constant.RestPath;
import xyz.erupt.core.service.CoreService;

import java.util.LinkedHashMap;

/**
 * Erupt 页面结构构建信息
 *
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
        EruptModel eruptModel = CoreService.getEruptView(eruptName);
        EruptBuildModel eruptBuildModel = new EruptBuildModel();
        eruptBuildModel.setEruptModel(eruptModel);
        for (EruptFieldModel fieldModel : eruptModel.getEruptFieldModels()) {
            switch (fieldModel.getEruptField().edit().type()) {
                case TAB_TREE:
                    if (eruptBuildModel.getTabErupts() == null) {
                        eruptBuildModel.setTabErupts(new LinkedHashMap<>());
                    }
                    EruptBuildModel eruptBuildModel1 = new EruptBuildModel();
                    eruptBuildModel1.setEruptModel(CoreService.getEruptView(fieldModel.getFieldReturnName()));
                    eruptBuildModel.getTabErupts().put(fieldModel.getFieldName(), eruptBuildModel1);
                    break;
                case TAB_TABLE_ADD:
                case TAB_TABLE_REFER:
                    if (eruptBuildModel.getTabErupts() == null) {
                        eruptBuildModel.setTabErupts(new LinkedHashMap<>());
                    }
                    eruptBuildModel.getTabErupts().put(fieldModel.getFieldName(), getEruptBuild(fieldModel.getFieldReturnName()));
                    break;
                case COMBINE:
                    if (eruptBuildModel.getCombineErupts() == null) {
                        eruptBuildModel.setCombineErupts(new LinkedHashMap<>());
                    }
                    eruptBuildModel.getCombineErupts().put(fieldModel.getFieldName(), CoreService.getEruptView(fieldModel.getFieldReturnName()));
                    break;
                case REFERENCE_TABLE:
                    if (eruptBuildModel.getReferenceErupts() == null) {
                        eruptBuildModel.setReferenceErupts(new LinkedHashMap<>());
                    }
                    eruptBuildModel.getReferenceErupts().put(fieldModel.getFieldName(), CoreService.getEruptView(fieldModel.getFieldReturnName()));
                    break;
                default:
                    break;
            }
        }
        for (RowOperation operation : eruptBuildModel.getEruptModel().getErupt().rowOperation()) {
            if (operation.eruptClass() != void.class) {
                if (eruptBuildModel.getOperationErupts() == null) {
                    eruptBuildModel.setOperationErupts(new LinkedHashMap<>());
                }
                eruptBuildModel.getOperationErupts().put(operation.code(), CoreService.getEruptView(operation.eruptClass().getSimpleName()));
            }
        }
//        for (Drill drill : eruptBuildModel.getEruptModel().getErupt().drills()) {
//            if (eruptBuildModel.getDrillErupts() == null) {
//                eruptBuildModel.setOperationErupts(new LinkedHashMap<>());
//            }
//            eruptBuildModel.getDrillErupts().put(drill.code(), CoreService.getErupt(drill.eruptClass().getSimpleName()));
//        }
        return eruptBuildModel;
    }

    @GetMapping("/{erupt}/{subErupt}")
    @ResponseBody
    @EruptRouter(authIndex = 1)
    public EruptBuildModel getEruptBuild(@PathVariable("erupt") String eruptName,
                                         @PathVariable("subErupt") String subEruupt) {
//        Class clazz = CoreService.getEruptClass(eruptName);


        return null;
    }


}
