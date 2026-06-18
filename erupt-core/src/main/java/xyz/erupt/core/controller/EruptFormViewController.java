package xyz.erupt.core.controller;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import xyz.erupt.core.util.ReflectUtil;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.R;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Form-view dedicated endpoints.
 * <p>
 * The form view is intentionally <b>data-source-agnostic</b>: the framework
 * only renders the form schema and routes the two lifecycle moments
 * ({@code formViewBehavior} and {@code formSave}) to {@link xyz.erupt.annotation.fun.DataProxy}.
 * Loading the data and persisting it are 100% the user's responsibility — it
 * can be a database, a file, an external API, anything.
 * <p>
 * Field-level validation and the existing {@code validate(model)} DataProxy
 * hook are reused (both fire inside {@link EruptUtil#validateEruptValue}),
 * because they are data-source-agnostic by nature.
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
     * <p>
     * Always constructs a fresh model instance. If {@code id} is supplied it is
     * coerced into the primary-key field of the model so the user can branch
     * inside {@code formViewBehavior} (e.g. {@code if (model.id == null) ... else ...})
     * — but the framework itself performs no database load. The user is
     * expected to populate the model from their own data source inside
     * {@code formViewBehavior}.
     */
    @GetMapping("/{erupt}")
    @SneakyThrows
    @EruptRouter(skipAuthIndex = 3, authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT)
    public Map<String, Object> openFormView(@PathVariable("erupt") String eruptName,
                                            @RequestParam(value = "id", required = false) String id) {
        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);
        Erupts.powerLegal(eruptModel, powerObject -> powerObject.isAdd() || powerObject.isEdit());
        Object model = eruptModel.getClazz().getDeclaredConstructor().newInstance();
        if (StringUtils.isNotBlank(id)) {
            Field pkField = ReflectUtil.findClassField(eruptModel.getClazz(), eruptModel.getErupt().primaryKeyCol());
            pkField.set(model, EruptUtil.toEruptId(eruptModel, id));
        }
        DataProxyInvoke.invoke(eruptModel, dataProxy -> dataProxy.formViewBehavior(model));
        return EruptUtil.generateEruptDataMap(eruptModel, model, false);
    }

    /**
     * Save the form view.
     * <p>
     * Runs field-level validation (which also fires {@code DataProxy.validate})
     * and then delegates the entire persistence action to {@code formSave}.
     * The framework intentionally does not invoke any database CRUD logic so
     * that file-backed, API-backed, or otherwise non-DB form views are
     * first-class. The user throws {@link EruptException} from
     * {@code formSave} to abort the save with a user-visible message.
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
