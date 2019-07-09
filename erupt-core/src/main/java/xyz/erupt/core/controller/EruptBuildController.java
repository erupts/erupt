package xyz.erupt.core.controller;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.bean.EruptBuildModel;
import xyz.erupt.core.bean.EruptFieldModel;
import xyz.erupt.core.bean.EruptModel;
import xyz.erupt.core.constant.RestPath;
import xyz.erupt.core.service.CoreService;

import javax.servlet.http.HttpServletResponse;

/**
 * Erupt 页面结构构建信息
 * Created by liyuepeng on 9/28/18.
 */
@RestController
@RequestMapping(RestPath.ERUPT_BUILD)
public class EruptBuildController {

    @GetMapping("/{erupt}")
    @ResponseBody
    @EruptRouter(base64 = true, authIndex = 1)
    public EruptBuildModel getEruptBuild(@PathVariable("erupt") String eruptName, HttpServletResponse response) {
        EruptModel eruptModel = CoreService.getErupt(eruptName);
        if (null == eruptModel) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        } else {
            EruptBuildModel eruptBuildModel = new EruptBuildModel();
            eruptBuildModel.setEruptModel(eruptModel);
            for (EruptFieldModel fieldModel : eruptModel.getEruptFieldModels()) {
                switch (fieldModel.getEruptField().edit().type()) {
                    case TAB_TABLE_ADD:
                    case TAB_TABLE_REFER:
                    case TAB_TREE:
                        eruptBuildModel.getTabErupts().put(fieldModel.getFieldName(), CoreService.getErupt(fieldModel.getFieldReturnName()));
                        break;
                    case COMBINE:
                        eruptBuildModel.getCombineErupts().put(fieldModel.getFieldName(), CoreService.getErupt(fieldModel.getFieldReturnName()));
                        break;
                    case REFERENCE_TABLE:
                        eruptBuildModel.getReferenceErupts().put(fieldModel.getFieldName(), CoreService.getErupt(fieldModel.getFieldReturnName()));
                        break;
                    default:
                        break;
                }
            }
            for (RowOperation operation : eruptBuildModel.getEruptModel().getErupt().rowOperation()) {
                if (operation.eruptClass() != void.class) {
                    eruptBuildModel.getOperationErupts().put(operation.code(), CoreService.getErupt(operation.eruptClass().getSimpleName()));
                }
            }
            return eruptBuildModel;
        }
    }

}
