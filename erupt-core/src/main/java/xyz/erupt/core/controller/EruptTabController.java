package xyz.erupt.core.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.invoke.DataProxyInvoke;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.R;

import java.util.function.Consumer;

/**
 * Tab rows
 *
 * @order 8
 * @apiNote Row editing inside TAB_TABLE_ADD fields: validation and DataProxy hooks of the child erupt without touching the database.
 *
 * @author YuePeng
 * date 2022/5/2 21:50
 */
@Slf4j
@RestController
@RequestMapping(EruptRestPath.ERUPT_DATA_MODIFY)
@RequiredArgsConstructor
public class EruptTabController {

    private final Gson gson = GsonFactory.getGson();

    /**
     * Validate a new child row.
     *
     * @apiNote Nothing is persisted here: the child rows are saved together with the parent record. This call validates the row against the child erupt and runs its DataProxy.beforeAdd/afterAdd, returning the possibly adjusted row.
     * @param erupt Simple class name of the parent @Erupt model
     * @param tabName Field name with edit type TAB_TABLE_ADD
     * @param data Child row values keyed by field name
     * @return validation result; data holds the row after the hooks
     */
    @PostMapping({"/tab-add/{erupt}/{tabName}"})
    @EruptRouter(skipAuthIndex = 3, authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT, power = {EruptRouter.Power.ADD, EruptRouter.Power.EDIT})
    public R<?> addTabEruptData(@PathVariable("erupt") String erupt, @PathVariable("tabName") String tabName, @RequestBody JsonObject data) {
        EruptModel eruptModel = getTabErupt(erupt, tabName);
        Object obj = gson.fromJson(data.toString(), eruptModel.getClazz());
        R<Object> r = this.tabValidate(eruptModel, data, dp -> {
            dp.beforeAdd(obj);
            dp.afterAdd(obj);
        });
        r.setData(obj);
        return r;
    }

    /**
     * Validate an edited child row.
     *
     * @apiNote Same as tab-add with the beforeUpdate/afterUpdate hooks.
     * @param erupt Simple class name of the parent @Erupt model
     * @param tabName Field name with edit type TAB_TABLE_ADD
     * @param data Child row values keyed by field name
     * @return validation result; data holds the row after the hooks
     */
    @PostMapping({"/tab-update/{erupt}/{tabName}"})
    @EruptRouter(skipAuthIndex = 3, authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT, power = {EruptRouter.Power.ADD, EruptRouter.Power.EDIT})
    public R<?> updateTabEruptData(@PathVariable("erupt") String erupt, @PathVariable("tabName") String tabName, @RequestBody JsonObject data) {
        EruptModel eruptModel = getTabErupt(erupt, tabName);
        Object obj = gson.fromJson(data.toString(), eruptModel.getClazz());
        R<Object> r = this.tabValidate(eruptModel, data, dp -> {
            dp.beforeUpdate(obj);
            dp.afterUpdate(obj);
        });
        r.setData(obj);
        return r;
    }

    /**
     * Notify the removal of a child row.
     *
     * @apiNote Runs the child erupt's beforeDelete/afterDelete hooks; the row itself is removed when the parent is saved.
     * @param erupt Simple class name of the parent @Erupt model
     * @param tabName Field name with edit type TAB_TABLE_ADD
     * @param data Child row values keyed by field name
     * @return ok
     */
    @PostMapping({"/tab-delete/{erupt}/{tabName}"})
    @EruptRouter(skipAuthIndex = 3, authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT, power = {EruptRouter.Power.ADD, EruptRouter.Power.EDIT})
    public R<Void> deleteTabEruptData(@PathVariable("erupt") String erupt, @PathVariable("tabName") String tabName, @RequestBody JsonObject data) {
        EruptModel eruptModel = getTabErupt(erupt, tabName);
        Object obj = gson.fromJson(data.toString(), eruptModel.getClazz());
        DataProxyInvoke.invoke(eruptModel, dp -> {
            dp.beforeDelete(obj);
            dp.afterDelete(obj);
        });
        return R.ok();
    }

    private <T> R<T> tabValidate(EruptModel eruptModel, JsonObject data, Consumer<DataProxy<Object>> consumer) {
        R<Void> validation = EruptUtil.validateEruptValue(eruptModel, data);
        if (validation.isSuccess()) {
            DataProxyInvoke.invoke(eruptModel, consumer);
        }
        R<T> r = new R<>();
        r.setSuccess(validation.isSuccess());
        r.setMessage(validation.getMessage());
        r.setStatus(validation.getStatus());
        r.setPromptWay(R.PromptWay.MESSAGE);
        return r;
    }

    private EruptModel getTabErupt(String erupt, String tabName) {
        EruptFieldModel tabField = EruptCoreService.getErupt(erupt).getEruptFieldMap().get(tabName);
        if (null == tabField) {
            throw new EruptWebApiRuntimeException(tabName + "not found");
        }
        return EruptCoreService.getErupt(tabField.getFieldReturnName());
    }


}
