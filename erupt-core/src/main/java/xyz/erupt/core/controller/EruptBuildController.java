package xyz.erupt.core.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.exception.EruptNoLegalPowerException;
import xyz.erupt.core.invoke.PowerInvoke;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.view.EruptBuildModel;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;

import java.util.LinkedHashMap;

/**
 * Erupt 页面结构构建信息
 *
 * @author YuePeng
 * date 2018-09-28.
 */
@RestController
@RequestMapping(EruptRestPath.ERUPT_BUILD)
public class EruptBuildController {

    @GetMapping("/{erupt}")
    @EruptRouter(authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT)
    public EruptBuildModel getEruptBuild(@PathVariable("erupt") String eruptName) {
        EruptModel eruptView = EruptCoreService.getEruptView(eruptName);
        EruptBuildModel eruptBuildModel = new EruptBuildModel();
        eruptBuildModel.setPower(PowerInvoke.getPowerObject(eruptView));
        eruptBuildModel.setEruptModel(eruptView);
        for (EruptFieldModel fieldModel : eruptView.getEruptFieldModels()) {
            switch (fieldModel.getEruptField().edit().type()) {
                case TAB_TREE:
                    if (eruptBuildModel.getTabErupts() == null) {
                        eruptBuildModel.setTabErupts(new LinkedHashMap<>());
                    }
                    EruptBuildModel eruptBuildModel1 = new EruptBuildModel();
                    eruptBuildModel1.setEruptModel(EruptCoreService.getEruptView(fieldModel.getFieldReturnName()));
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
                    eruptBuildModel.getCombineErupts().put(fieldModel.getFieldName(), EruptCoreService.getEruptView(fieldModel.getFieldReturnName()));
                    break;
                default:
                    break;
            }
        }
        for (RowOperation operation : eruptBuildModel.getEruptModel().getErupt().rowOperation()) {
            if (operation.eruptClass() != void.class && operation.type() == RowOperation.Type.ERUPT) {
                if (eruptBuildModel.getOperationErupts() == null) {
                    eruptBuildModel.setOperationErupts(new LinkedHashMap<>());
                }
                eruptBuildModel.getOperationErupts().put(operation.code(), EruptCoreService.getEruptView(operation.eruptClass().getSimpleName()));
            }
        }
        return eruptBuildModel;
    }

    @GetMapping("/{erupt}/{field}")
    @EruptRouter(authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT)
    public EruptBuildModel getEruptBuild(@PathVariable("erupt") String eruptName, @PathVariable("field") String field) {
        EruptModel eruptModel = EruptCoreService.getEruptView(eruptName);
        EruptFieldModel eruptFieldModel = eruptModel.getEruptFieldMap().get(field);
        if (null != eruptFieldModel) {
            return this.getEruptBuild(eruptFieldModel.getFieldReturnName());
        }
        throw new EruptNoLegalPowerException();
    }

}
