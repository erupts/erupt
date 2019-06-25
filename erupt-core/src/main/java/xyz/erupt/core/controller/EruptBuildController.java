package xyz.erupt.core.controller;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.RestPath;
import xyz.erupt.core.model.EruptAndEruptFieldModel;
import xyz.erupt.core.model.EruptBuildModel;
import xyz.erupt.core.model.EruptFieldModel;
import xyz.erupt.core.model.EruptModel;
import xyz.erupt.core.service.CoreService;
import xyz.erupt.core.util.ReflectUtil;

import javax.servlet.http.HttpServletResponse;

/**
 * Erupt 页面结构构建信息
 * Created by liyuepeng on 9/28/18.
 */
@RestController
@RequestMapping(RestPath.ERUPT_BUILD)
public class EruptBuildController {

    @GetMapping("/list/{erupt}")
    @ResponseBody
    @EruptRouter(base64 = true, verifyIndex = 1)
    public EruptBuildModel getEruptTableView(@PathVariable("erupt") String eruptName, HttpServletResponse response) {
        EruptModel eruptModel = CoreService.getErupt(eruptName);
        if (null == eruptModel) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        } else {
            EruptBuildModel eruptBuildModel = new EruptBuildModel();
            eruptBuildModel.setEruptModel(eruptModel);
            for (EruptFieldModel fieldModel : eruptModel.getEruptFieldModels()) {
                EruptModel em;
                switch (fieldModel.getEruptField().edit().type()) {
                    case TAB:
                        em = CoreService.getErupt(ReflectUtil.getFieldGenericName(fieldModel.getField()).get(0));
                        eruptBuildModel.getSubErupts().add(new EruptAndEruptFieldModel(fieldModel, em));
                        break;
                    case COMBINE:
                        em = CoreService.getErupt(fieldModel.getFieldReturnName());
                        eruptBuildModel.getCombineErupts().put(fieldModel.getFieldName(), em);
                        break;
                    case REFERENCE_TABLE:
                        em = CoreService.getErupt(fieldModel.getFieldReturnName());
                        eruptBuildModel.getReferenceErupts().put(fieldModel.getFieldName(), em);
                        break;
                }
            }
            return eruptBuildModel;
        }
    }

}
