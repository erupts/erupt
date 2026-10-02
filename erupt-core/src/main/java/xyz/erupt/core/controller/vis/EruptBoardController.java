package xyz.erupt.core.controller.vis;

import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.annotation.Vis;
import xyz.erupt.annotation.fun.PowerObject;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.invoke.DataProxyInvoke;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.core.util.ReflectUtil;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.R;

import java.lang.reflect.Field;

/**
 * @author YuePeng
 * date 2025/11/15 00:25
 */
@Slf4j
@RestController
@RequestMapping(EruptRestPath.ERUPT_DATA_MODIFY + "/board")
@RequiredArgsConstructor
public class EruptBoardController {

    @PostMapping("/{erupt}/update_group")
    @EruptRouter(skipAuthIndex = 4, authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT)
    @Transactional
    @SneakyThrows
    public R<Void> updateGroup(@PathVariable("erupt") String erupt, @RequestBody VisBoardGroupCommand command) {
        EruptModel eruptModel = EruptCoreService.getErupt(erupt);
        Erupts.powerLegal(eruptModel, PowerObject::isEdit);
        for (Vis vis : eruptModel.getErupt().vis()) {
            if (vis.code().equals(command.getVisCode())) {
                Object obj = DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz()).findDataById(eruptModel, command.getPk());
                this.assign(eruptModel, obj, vis.boardView().groupField(), command.getGroupValue());
                // a card dropped into another swimlane changes the second grouping as well
                if (command.isSwimlaneChanged() && !vis.boardView().swimlaneField().isEmpty()) {
                    this.assign(eruptModel, obj, vis.boardView().swimlaneField(), command.getSwimlaneValue());
                }
                DataProxyInvoke.invoke(eruptModel, (dataProxy -> dataProxy.beforeUpdate(obj)));
                DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz()).editData(eruptModel, obj);
                DataProxyInvoke.invoke(eruptModel, (dataProxy -> dataProxy.afterUpdate(obj)));
                break;
            }
        }
        return R.ok();
    }

    /**
     * Write a raw board value onto the entity; reference fields get the referenced entity
     * looked up by id so JPA can persist the association.
     */
    @SneakyThrows
    private void assign(EruptModel eruptModel, Object obj, String fieldName, Object value) {
        Field field = ReflectUtil.findClassField(obj.getClass(), fieldName);
        EruptFieldModel fieldModel = eruptModel.getEruptFieldMap().get(fieldName);
        if (null != fieldModel && null != value) {
            EditType editType = fieldModel.getEruptField().edit().type();
            if (editType == EditType.REFERENCE_TREE || editType == EditType.REFERENCE_TABLE) {
                EruptModel refModel = EruptCoreService.getErupt(fieldModel.getFieldReturnName());
                value = DataProcessorManager.getEruptDataProcessor(refModel.getClazz()).findDataById(refModel, value);
            }
        }
        field.set(obj, value);
    }

    @Getter
    @Setter
    public static class VisBoardGroupCommand {

        private String visCode;

        private Object pk;

        private Object groupValue;

        // only meaningful when swimlaneChanged is set: null clears the lane
        private Object swimlaneValue;

        private boolean swimlaneChanged;

    }

}
