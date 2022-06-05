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
import xyz.erupt.core.view.EruptApiModel;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;

import java.util.function.Consumer;

/**
 * @author YuePeng
 * date 2022/5/2 21:50
 */
@Slf4j
@RestController
@RequestMapping(EruptRestPath.ERUPT_DATA_MODIFY)
@RequiredArgsConstructor
public class EruptTabController {

    private final Gson gson = GsonFactory.getGson();

    //TAB组件新增行为
    @PostMapping({"/tab-add/{erupt}/{tabName}"})
    @EruptRouter(skipAuthIndex = 3, authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT)
    public EruptApiModel addTabEruptData(@PathVariable("erupt") String erupt, @PathVariable("tabName") String tabName, @RequestBody JsonObject data) {
        EruptModel eruptModel = getTabErupt(erupt, tabName);
        Object obj = gson.fromJson(data.toString(), eruptModel.getClazz());
        EruptApiModel eruptApiModel = this.tabValidate(eruptModel, data, dp -> dp.beforeAdd(obj));
        eruptApiModel.setData(obj);
        return eruptApiModel;
    }

    //TAB组件更新行为
    @PostMapping({"/tab-update/{erupt}/{tabName}"})
    @EruptRouter(skipAuthIndex = 3, authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT)
    public EruptApiModel updateTabEruptData(@PathVariable("erupt") String erupt, @PathVariable("tabName") String tabName, @RequestBody JsonObject data) {
        EruptModel eruptModel = getTabErupt(erupt, tabName);
        Object obj = gson.fromJson(data.toString(), eruptModel.getClazz());
        EruptApiModel eruptApiModel = this.tabValidate(eruptModel, data, dp -> dp.beforeUpdate(obj));
        eruptApiModel.setData(obj);
        return eruptApiModel;
    }

    //TAB组件删除行为
    @PostMapping({"/tab-delete/{erupt}/{tabName}"})
    @EruptRouter(skipAuthIndex = 3, authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT)
    public EruptApiModel deleteTabEruptData(@PathVariable("erupt") String erupt, @PathVariable("tabName") String tabName, @RequestBody JsonObject data) {
        EruptApiModel eruptApiModel = EruptApiModel.successApi();
        EruptModel eruptModel = getTabErupt(erupt, tabName);
        DataProxyInvoke.invoke(eruptModel, dp -> dp.beforeDelete(gson.fromJson(data.toString(), eruptModel.getClazz())));
        eruptApiModel.setPromptWay(EruptApiModel.PromptWay.MESSAGE);
        return eruptApiModel;
    }

    private EruptApiModel tabValidate(EruptModel eruptModel, JsonObject data, Consumer<DataProxy<Object>> consumer) {
        EruptApiModel eruptApiModel = EruptUtil.validateEruptValue(eruptModel, data);
        if (eruptApiModel.getStatus() == EruptApiModel.Status.SUCCESS) {
            DataProxyInvoke.invoke(eruptModel, consumer);
        }
        eruptApiModel.setErrorIntercept(false);
        eruptApiModel.setPromptWay(EruptApiModel.PromptWay.MESSAGE);
        return eruptApiModel;
    }

    private EruptModel getTabErupt(String erupt, String tabName) {
        EruptFieldModel tabField = EruptCoreService.getErupt(erupt).getEruptFieldMap().get(tabName);
        if (null == tabField) {
            throw new EruptWebApiRuntimeException(tabName + "not found");
        }
        return EruptCoreService.getErupt(tabField.getFieldReturnName());
    }


}
