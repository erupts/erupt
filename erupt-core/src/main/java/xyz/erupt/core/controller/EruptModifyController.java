package xyz.erupt.core.controller;

import com.google.gson.JsonObject;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.annotation.fun.PowerObject;
import xyz.erupt.core.annotation.EruptRecordOperate;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.event.EruptAddEvent;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.invoke.DataProxyInvoke;
import xyz.erupt.core.naming.EruptRecordNaming;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.service.EruptModifyService;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.core.view.EruptCellVo;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.R;

import java.util.List;
import java.util.Map;

/**
 * Data modify
 *
 * @order 7
 * @apiNote Write side of erupt data: insert, update, delete and ordering.
 *
 * @author YuePeng
 * date 9/28/18.
 */
@Slf4j
@RestController
@RequestMapping(EruptRestPath.ERUPT_DATA_MODIFY)
@RequiredArgsConstructor
public class EruptModifyController {

    private final EruptModifyService eruptModifyService;

    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * Insert a record.
     *
     * @apiNote Values are validated against the field definitions, then DataProxy.beforeAdd/afterAdd run around the insert. Reference fields are sent as {"id": ...}.
     * @param erupt Simple class name of the @Erupt model
     * @param data Record values keyed by field name
     * @return ok when inserted
     */
    @PostMapping({"/{erupt}"})
    @EruptRecordOperate(value = "INSERT", dynamicConfig = EruptRecordNaming.class)
    @EruptRouter(skipAuthIndex = 3, authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT, highSafe = true, power = EruptRouter.Power.ADD, payload = EruptRouter.Payload.RECORD)
    public R<Void> addEruptData(@PathVariable("erupt") String erupt, @RequestBody JsonObject data) {
        EruptModel eruptModel = EruptCoreService.getErupt(erupt);
        Erupts.powerLegal(eruptModel, PowerObject::isAdd);
        eruptModifyService.insertEruptData(eruptModel, data);
        return R.ok();
    }

    @SneakyThrows
    public void batchAddEruptData(EruptModel eruptModel, List<Object> list) {
        for (Object data : list) {
            DataProxyInvoke.invoke(eruptModel, (dataProxy -> dataProxy.beforeAdd(data)));
        }
        DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz()).batchAddData(eruptModel, list);
        for (Object obj : list) {
            eruptModifyService.modifyLog(eruptModel, "ADD", EruptUtil.toMaskedJson(eruptModel, obj));
            DataProxyInvoke.invoke(eruptModel, (dataProxy -> dataProxy.afterAdd(obj)));
            applicationEventPublisher.publishEvent(new EruptAddEvent<>(eruptModel.getClazz(), obj));
        }
    }

    /**
     * Update a record.
     *
     * @apiNote The primary key field selects the row. Fields absent from the body keep their value; DataProxy.beforeUpdate/afterUpdate run around the update.
     * @param erupt Simple class name of the @Erupt model
     * @param data Record values keyed by field name, including the primary key
     * @return ok when updated
     */
    @PostMapping("/{erupt}/update")
    @EruptRecordOperate(value = "UPDATE", dynamicConfig = EruptRecordNaming.class)
    @EruptRouter(skipAuthIndex = 3, authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT, highSafe = true, power = EruptRouter.Power.EDIT, payload = EruptRouter.Payload.RECORD)
    @Transactional
    @SneakyThrows
    public R<Void> updateEruptData(@PathVariable("erupt") String erupt, @RequestBody JsonObject data) {
        EruptModel eruptModel = EruptCoreService.getErupt(erupt);
        Erupts.powerLegal(eruptModel, PowerObject::isEdit);
        eruptModifyService.updateEruptData(eruptModel, data);
        return R.ok();
    }

    /**
     * Update one field of one row.
     *
     * @apiNote In-table cell editing; only allowed when the model opts in with cellEdit. Goes through the full edit pipeline, so validation and DataProxy hooks apply.
     * @param erupt Simple class name of the @Erupt model
     * @param cell Primary key, field name and new value
     * @return ok when updated
     */
    @PostMapping("/{erupt}/update-cell")
    @EruptRecordOperate(value = "UPDATE", dynamicConfig = EruptRecordNaming.class)
    @EruptRouter(skipAuthIndex = 3, authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT, highSafe = true, power = EruptRouter.Power.EDIT,
            resolve = EruptRouter.Resolve.CELL)
    @Transactional
    @SneakyThrows
    public R<Void> updateEruptCell(@PathVariable("erupt") String erupt, @RequestBody EruptCellVo cell) {
        EruptModel eruptModel = EruptCoreService.getErupt(erupt);
        Erupts.powerLegal(eruptModel, PowerObject::isEdit);
        // cell editing is opt-in per model; a crafted request must not bypass that
        Erupts.powerLegal(eruptModel, PowerObject::isCellEdit);
        eruptModifyService.updateEruptCell(eruptModel, cell.getId(), cell.getField(), cell.getValue());
        return R.ok();
    }

    /**
     * Delete records by primary key.
     *
     * @apiNote DataProxy.beforeDelete/afterDelete run for every row.
     * @param erupt Simple class name of the @Erupt model
     * @param ids Primary keys to delete
     * @return ok when deleted
     */
    @PostMapping("/{erupt}/delete")
    @EruptRouter(skipAuthIndex = 3, authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT, highSafe = true, power = EruptRouter.Power.DELETE)
    @EruptRecordOperate(value = "DELETE", dynamicConfig = EruptRecordNaming.class)
    @Transactional
    public R<Void> deleteEruptData(@PathVariable("erupt") String erupt, @RequestBody List<Object> ids) {
        EruptModel eruptModel = EruptCoreService.getErupt(erupt);
        Erupts.powerLegal(eruptModel, PowerObject::isDelete);
        eruptModifyService.deleteEruptData(eruptModel, ids, true);
        return R.ok();
    }


    /**
     * Persist a drag and drop reorder.
     *
     * @apiNote Body maps primary key to its new position in the @Erupt.dragSort field.
     * @param erupt Simple class name of the @Erupt model
     * @param sortData Primary key to new sort value
     * @return ok when saved
     */
    @PostMapping("/{erupt}/drag-sort")
    @EruptRecordOperate(value = "UPDATE", dynamicConfig = EruptRecordNaming.class)
    @EruptRouter(skipAuthIndex = 3, authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT, highSafe = true, power = EruptRouter.Power.EDIT)
    public R<Void> dragSortEruptData(@PathVariable("erupt") String erupt, @RequestBody Map<String, Object> sortData) {
        EruptModel eruptModel = EruptCoreService.getErupt(erupt);
        Erupts.powerLegal(eruptModel, PowerObject::isEdit);
        eruptModifyService.dragSortEruptData(eruptModel, sortData);
        return R.ok();
    }

}
