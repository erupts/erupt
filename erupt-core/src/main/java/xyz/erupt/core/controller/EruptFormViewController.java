package xyz.erupt.core.controller;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.annotation.exception.EruptException;
import xyz.erupt.core.annotation.EruptRecordOperate;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.exception.EruptApiErrorTip;
import xyz.erupt.core.invoke.DataProxyInvoke;
import xyz.erupt.core.naming.EruptRecordNaming;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.R;

import java.util.Map;

/**
 * Form view
 *
 * @apiNote Form view: a standalone form whose loading and saving belong entirely to the user.
 * The framework only renders the form schema and routes the two lifecycle moments, DataProxy.formViewBehavior and DataProxy.formSave, to the DataProxy. Loading the data and persisting it are the user's responsibility: a database, a file, an external API, anything. Field-level validation and the DataProxy.validate hook are reused because they are data-source-agnostic by nature.
 *
 * @author YuePeng
 */
@Slf4j
@RestController
@RequestMapping(EruptRestPath.ERUPT_DATA + "/form-view")
@RequiredArgsConstructor
public class EruptFormViewController {

    /**
     * Open the form view.
     *
     * @apiNote Always constructs a fresh model instance and runs DataProxy.formViewBehavior on it; the framework performs no database load, the user populates the model from their own data source inside that hook.
     * @param eruptName Simple class name of the @Erupt model
     * @return field name to value
     */
    @GetMapping("/{erupt}")
    @SneakyThrows
    @EruptRouter(skipAuthIndex = 3, authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT)
    public Map<String, Object> openFormView(@PathVariable("erupt") String eruptName) {
        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);
        Erupts.powerLegal(eruptModel, powerObject -> powerObject.isAdd() || powerObject.isEdit());
        Object model = eruptModel.getClazz().getDeclaredConstructor().newInstance();
        DataProxyInvoke.invoke(eruptModel, dataProxy -> dataProxy.formViewBehavior(model));
        return EruptUtil.generateEruptDataMap(eruptModel, model, false);
    }

    /**
     * Save the form view.
     *
     * @apiNote Runs field-level validation (which also fires DataProxy.validate) and then delegates the entire persistence action to DataProxy.formSave. No database CRUD is invoked by the framework, so file-backed or API-backed form views are first-class. Throwing EruptException from formSave aborts the save with a user-visible message.
     * @param eruptName Simple class name of the @Erupt model
     * @param data Form values keyed by field name
     * @return ok when saved
     */
    @PostMapping("/{erupt}")
    @EruptRecordOperate(value = "FORM-VIEW", dynamicConfig = EruptRecordNaming.class)
    @EruptRouter(skipAuthIndex = 3, authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT, highSafe = true)
    @SneakyThrows
    public R<Void> saveFormView(@PathVariable("erupt") String eruptName, @RequestBody JsonObject data) {
        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);
        Erupts.powerLegal(eruptModel, powerObject -> powerObject.isAdd() || powerObject.isEdit());
        R<Void> validation = EruptUtil.validateEruptValue(eruptModel, data);
        if (!validation.isSuccess()) {
            throw new EruptApiErrorTip(validation.getMessage(), R.PromptWay.MESSAGE);
        }
        Object model = GsonFactory.getGson().fromJson(data, eruptModel.getClazz());
        DataProxyInvoke.invoke(eruptModel, dataProxy -> {
            try {
                dataProxy.formSave(model);
            } catch (EruptException e) {
                throw new EruptApiErrorTip(e.getMessage(), R.PromptWay.MESSAGE);
            }
        });
        return R.ok();
    }
}
