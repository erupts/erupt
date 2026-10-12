package xyz.erupt.core.controller;


import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.invoke.DataProxyInvoke;
import xyz.erupt.core.invoke.PowerInvoke;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.util.ReflectUtil;
import xyz.erupt.core.view.EruptBuildModel;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;

import java.util.*;

/**
 * Page build
 *
 * @apiNote Page structure of an @Erupt model: fields, edit/view types, operations and the caller's permissions.
 *
 * @author YuePeng
 * date 2018-09-28.
 */
@RestController
@RequestMapping(EruptRestPath.ERUPT_BUILD)
public class EruptBuildController {

    /**
     * Build model of an erupt.
     *
     * @apiNote Everything the frontend needs to render the table, search bar, form and buttons of one @Erupt class: field metadata, default search conditions, the current user's PowerObject, plus the build models of nested erupts (tab, multi-form, combine) and of row operations that open another erupt as a form.
     * @param eruptName Simple class name of the @Erupt model
     * @return build model
     */
    @GetMapping("/{erupt}")
    @EruptRouter(authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT)
    @SneakyThrows
    public EruptBuildModel getEruptBuild(@PathVariable("erupt") String eruptName) {
        EruptModel eruptView = EruptCoreService.getEruptView(eruptName);
        {
            //default search conditions
            Map<String, Object> conditionsMap = new HashMap<>();
            DataProxyInvoke.invoke(eruptView, it -> it.searchCondition(conditionsMap));
            eruptView.setSearchCondition(conditionsMap);
        }
        EruptBuildModel eruptBuildModel = new EruptBuildModel();
        eruptBuildModel.setPower(PowerInvoke.getPowerObject(eruptView));
        eruptBuildModel.setEruptModel(eruptView);
        eruptView.getEruptFieldModels().forEach(fieldModel -> {
            switch (fieldModel.getEruptField().edit().type()) {
                case TAB_TREE:
                    eruptBuildModel.setTabErupts(Optional.ofNullable(eruptBuildModel.getTabErupts()).orElse(new LinkedHashMap<>()));
                    EruptBuildModel m1 = new EruptBuildModel();
                    m1.setEruptModel(EruptCoreService.getEruptView(fieldModel.getFieldReturnName()));
                    eruptBuildModel.getTabErupts().put(fieldModel.getFieldName(), m1);
                    break;
                case TAB_TABLE_ADD:
                case MULTI_FORM:
                case TAB_TABLE_REFER:
                    eruptBuildModel.setTabErupts(Optional.ofNullable(eruptBuildModel.getTabErupts()).orElse(new LinkedHashMap<>()));
                    eruptBuildModel.getTabErupts().put(fieldModel.getFieldName(), getEruptBuild(fieldModel.getFieldReturnName()));
                    break;
                case COMBINE:
                    eruptBuildModel.setCombineErupts(Optional.ofNullable(eruptBuildModel.getCombineErupts()).orElse(new LinkedHashMap<>()));
                    eruptBuildModel.getCombineErupts().put(fieldModel.getFieldName(), EruptCoreService.getEruptView(fieldModel.getFieldReturnName()));
                    break;
            }
        });
        Arrays.stream(eruptBuildModel.getEruptModel().getErupt().rowOperation()).filter(operation ->
                operation.eruptClass() != void.class && operation.type() == RowOperation.Type.ERUPT).forEach(operation -> {
            eruptBuildModel.setOperationErupts(Optional.ofNullable(eruptBuildModel.getOperationErupts()).orElse(new LinkedHashMap<>()));
            eruptBuildModel.getOperationErupts().put(operation.code(), EruptCoreService.getEruptView(operation.eruptClass().getSimpleName()));
        });
        return eruptBuildModel;
    }

    /**
     * Build model of the erupt referenced by a field.
     *
     * @apiNote Resolves the target class of a reference, tab or link-tree field and returns its build model, so a nested erupt can be rendered without knowing its class name up front.
     * @param eruptName Simple class name of the @Erupt model
     * @param field Field name on the erupt; its declared type is the erupt to build
     * @return build model of the referenced erupt
     */
    @GetMapping("/{erupt}/{field}")
    @EruptRouter(authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT)
    public EruptBuildModel getEruptBuildByField(@PathVariable("erupt") String eruptName, @PathVariable("field") String field) {
        EruptModel eruptModel = EruptCoreService.getEruptView(eruptName);
        EruptFieldModel fieldModel = eruptModel.getEruptFieldMap().get(field);
        // The field may lack @EruptField (e.g. LinkTree.field), fall back to reflection like getDependTree does
        String treeErupt = null != fieldModel ? fieldModel.getFieldReturnName()
                : ReflectUtil.findClassField(eruptModel.getClazz(), field).getType().getSimpleName();
        return this.getEruptBuild(treeErupt);
    }

}
