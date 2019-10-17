package xyz.erupt.core.controller;


import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.bean.EruptBuildModel;
import xyz.erupt.core.bean.EruptFieldModel;
import xyz.erupt.core.bean.EruptModel;
import xyz.erupt.core.constant.RestPath;
import xyz.erupt.core.service.CoreService;

import java.io.IOException;
import java.util.HashMap;

/**
 * Erupt 页面结构构建信息
 * Created by liyuepeng on 9/28/18.
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
                        eruptBuildModel.setTabErupts(new HashMap<>());
                    }
                    EruptBuildModel eruptBuildModel1 = new EruptBuildModel();
                    eruptBuildModel1.setEruptModel(CoreService.getErupt(fieldModel.getFieldReturnName()));
                    eruptBuildModel.getTabErupts().put(fieldModel.getFieldName(), eruptBuildModel1);
                    break;
                case TAB_TABLE_ADD:
                case TAB_TABLE_REFER:
                    if (eruptBuildModel.getTabErupts() == null) {
                        eruptBuildModel.setTabErupts(new HashMap<>());
                    }
                    eruptBuildModel.getTabErupts().put(fieldModel.getFieldName(), getEruptBuild(fieldModel.getFieldReturnName()));
                    break;
                case COMBINE:
                    if (eruptBuildModel.getCombineErupts() == null) {
                        eruptBuildModel.setCombineErupts(new HashMap<>());
                    }
                    eruptBuildModel.getCombineErupts().put(fieldModel.getFieldName(), CoreService.getErupt(fieldModel.getFieldReturnName()));
                    break;
                case REFERENCE_TABLE:
                    if (eruptBuildModel.getReferenceErupts() == null) {
                        eruptBuildModel.setReferenceErupts(new HashMap<>());
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
                    eruptBuildModel.setOperationErupts(new HashMap<>());
                }
                eruptBuildModel.getOperationErupts().put(operation.code(), CoreService.getErupt(operation.eruptClass().getSimpleName()));
            }
        }
        return eruptBuildModel;
    }


    @GetMapping(value = "/html-field/{erupt}/{field}", produces = {"text/html;charset=utf-8"})
    @ResponseBody
    @EruptRouter(authIndex = 2)
    public String getEruptFieldHtml(@PathVariable("erupt") String eruptName, @PathVariable("field") String field) throws IOException {
        EruptModel eruptModel = CoreService.getErupt(eruptName);
        String path = eruptModel.getEruptFieldMap().get(field).getEruptField().edit().htmlType().path();
        Resource resource = new ClassPathResource(path);
        return FileUtils.readFileToString(resource.getFile());
    }

    @GetMapping(value = "/html/{erupt}/{field}", produces = {"text/html;charset=utf-8"})
    @ResponseBody
    @EruptRouter(authIndex = 2)
    public String getEruptHtml(@PathVariable("erupt") String eruptName, @PathVariable("field") String field) throws IOException {
        EruptModel eruptModel = CoreService.getErupt(eruptName);
        String path = eruptModel.getErupt().beforeHtml().path();
        Resource resource = new ClassPathResource(path);
        return FileUtils.readFileToString(resource.getFile());
    }
}
