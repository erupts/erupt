package xyz.erupt.core.controller;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.annotation.fun.CodeEditHintHandler;
import xyz.erupt.annotation.fun.EruptButtonHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.sub_edit.AutoCompleteType;
import xyz.erupt.annotation.sub_field.sub_edit.ButtonType;
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.exception.EruptApiErrorTip;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.view.EruptButtonVo;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Form components
 *
 * @order 10
 * @apiNote Data sources for form widgets: auto-complete, choice, tags, mention, button and code hints.
 *
 * @author YuePeng
 * date 2020-08-14
 */
@RestController
@RequestMapping(EruptRestPath.ERUPT_COMP)
public class EruptComponentController {

    /**
     * Auto-complete suggestions for an AUTO_COMPLETE field.
     *
     * @apiNote Static values from @AutoCompleteType.values are matched case-insensitively, then the configured handler adds its own. The input must be at least triggerLength characters.
     * @param eruptName Simple class name of the @Erupt model
     * @param field Field name with edit type AUTO_COMPLETE
     * @param val Text typed so far
     * @param data Current form values keyed by field name
     * @return suggestions
     */
    @SuppressWarnings("unchecked")
    @PostMapping("/auto-complete/{erupt}/{field}")
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT, resolve = EruptRouter.Resolve.AUTO_COMPLETE)
    public List<Object> autoCompleteValue(@PathVariable("erupt") String eruptName,
                                          @PathVariable("field") String field,
                                          @RequestParam("val") String val,
                                          @RequestBody JsonObject data) {
        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);
        EruptFieldModel fieldModel = eruptModel.getEruptFieldMap().get(field);
        Object o = GsonFactory.getGson().fromJson(data.toString(), eruptModel.getClazz());
        AutoCompleteType autoCompleteType = fieldModel.getEruptField().edit().autoCompleteType();
        if (val.length() < autoCompleteType.triggerLength()) {
            throw new EruptWebApiRuntimeException("char length must >= " + autoCompleteType.triggerLength());
        }
        List<Object> result = new ArrayList<>();
        String keyword = val.toLowerCase();
        for (String value : autoCompleteType.values()) {
            if (value.toLowerCase().contains(keyword)) result.add(value);
        }
        if (!autoCompleteType.handler().isInterface()) {
            try {
                result.addAll(EruptSpringUtil.getBean(autoCompleteType.handler()).completeHandler(o, val, autoCompleteType.param()));
            } catch (Exception e) {
                throw new EruptApiErrorTip(e.getMessage(), R.PromptWay.MESSAGE);
            }
        }
        return result;
    }

    /**
     * Options of a CHOICE field.
     *
     * @apiNote Static @ChoiceType.vl entries plus whatever the ChoiceFetchHandler returns.
     * @param eruptName Simple class name of the @Erupt model
     * @param field Field name with edit type CHOICE
     * @return value/label pairs
     */
    @GetMapping("/choice-item/{erupt}/{field}")
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT, resolve = EruptRouter.Resolve.OPTIONS)
    public List<VLModel> choiceItem(@PathVariable("erupt") String eruptName,
                                    @PathVariable("field") String field) {
        // an erupt-cloud node erupt's static options are answered here, from its node's build
        EruptModel eruptModel = EruptCoreService.getEruptWithRemoteModel(eruptName);
        EruptFieldModel fieldModel = eruptModel.getEruptFieldMap().get(field);
        return EruptUtil.getChoiceList(eruptModel, fieldModel);
    }

    /**
     * Options of a CHOICE field filtered by the current form.
     *
     * @apiNote Same as choice-item but the ChoiceFetchHandler sees the form values, so options can depend on other fields.
     * @param eruptName Simple class name of the @Erupt model
     * @param field Field name with edit type CHOICE
     * @param data Current form values keyed by field name
     * @return value/label pairs
     */
    @PostMapping("/choice-item-filter/{erupt}/{field}")
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT, resolve = EruptRouter.Resolve.OPTIONS)
    public List<VLModel> choiceFilter(@PathVariable("erupt") String eruptName,
                                      @PathVariable("field") String field,
                                      @RequestBody JsonObject data) {
        EruptModel eruptModel = EruptCoreService.getEruptWithRemoteModel(eruptName);
        EruptFieldModel fieldModel = eruptModel.getEruptFieldMap().get(field);
        // a node erupt reaches here only for options no handler fetches: the form cannot change them
        if (eruptModel.isRemote()) return EruptUtil.getChoiceList(eruptModel, fieldModel);
        Object o = GsonFactory.getGson().fromJson(data.toString(), eruptModel.getClazz());
        return EruptUtil.getChoiceListFilter(eruptModel, fieldModel, o);
    }

    /**
     * Tag suggestions for a TAGS field.
     *
     * @param eruptName Simple class name of the @Erupt model
     * @param field Field name with edit type TAGS
     * @param data Current form values keyed by field name
     * @return tags
     */
    @PostMapping("/tags-item/{erupt}/{field}")
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT, resolve = EruptRouter.Resolve.OPTIONS)
    public List<String> tagsItem(@PathVariable("erupt") String eruptName,
                                 @PathVariable("field") String field,
                                 @RequestBody JsonObject data
    ) {
        EruptModel eruptModel = EruptCoreService.getEruptWithRemoteModel(eruptName);
        EruptFieldModel fieldModel = eruptModel.getEruptFieldMap().get(field);
        // a node erupt reaches here only for tags no handler fetches: its build's tags() are the answer
        Object o = eruptModel.isRemote() ? null : GsonFactory.getGson().fromJson(data.toString(), eruptModel.getClazz());
        List<String> tags = EruptUtil.getTagList(fieldModel.getEruptField().edit().tagsType(), o);
        // a node whose build leaves tags() out (an older erupt, the language SDKs) sends them as the componentValue
        if (eruptModel.isRemote() && tags.isEmpty() && null != fieldModel.getComponentValue()) {
            return GsonFactory.getGson().fromJson(GsonFactory.getGson().toJsonTree(fieldModel.getComponentValue()),
                    new TypeToken<List<String>>() {
                    }.getType());
        }
        return tags;
    }

    /**
     * Mention suggestions for a TEXTAREA field.
     *
     * @param eruptName Simple class name of the @Erupt model
     * @param field Field name with edit type TEXTAREA
     * @param data Current form values keyed by field name
     * @return mention candidates
     */
    @PostMapping("/textarea-mention/{erupt}/{field}")
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT)
    public List<String> textareaMention(@PathVariable("erupt") String eruptName,
                                        @PathVariable("field") String field,
                                        @RequestBody JsonObject data
    ) {
        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);
        EruptFieldModel fieldModel = eruptModel.getEruptFieldMap().get(field);
        Object o = GsonFactory.getGson().fromJson(data.toString(), eruptModel.getClazz());
        return EruptUtil.getMentionList(fieldModel.getEruptField().edit().textareaType(), o);
    }

    /**
     * Click of a BUTTON field.
     *
     * @apiNote Hands the whole form to the EruptButtonHandler and returns what it wants the frontend to do: a script to evaluate, form values to populate and edit expressions to apply.
     * @param eruptName Simple class name of the @Erupt model
     * @param field Field name with edit type BUTTON
     * @param data Current form values keyed by field name
     * @return handler result
     */
    @SuppressWarnings("unchecked")
    @PostMapping("/button/{erupt}/{field}")
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT, resolve = EruptRouter.Resolve.BUTTON)
    public R<EruptButtonVo> buttonClick(@PathVariable("erupt") String eruptName,
                                        @PathVariable("field") String field,
                                        @RequestBody JsonObject data) {
        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);
        EruptFieldModel fieldModel = eruptModel.getEruptFieldMap().get(field);
        if (EditType.BUTTON != fieldModel.getEruptField().edit().type()) {
            throw new EruptWebApiRuntimeException(field + " is not a BUTTON component");
        }
        ButtonType buttonType = fieldModel.getEruptField().edit().buttonType();
        if (buttonType.handler().isInterface()) {
            return R.errorDialog("Please implement the 'EruptButtonHandler' interface for " + fieldModel.getEruptField().edit().title());
        }
        Object form = GsonFactory.getGson().fromJson(data.toString(), eruptModel.getClazz());
        EruptButtonHandler<Object> handler = EruptSpringUtil.getBean(buttonType.handler());
        String[] handlerParams = buttonType.handlerParams();
        EruptButtonVo buttonVo = new EruptButtonVo();
        buttonVo.setEval(handler.click(form, handlerParams));
        buttonVo.setFormData(handler.populateForm(form, handlerParams));
        buttonVo.setEditExpr(handler.buildEditExpr(form, handlerParams));
        return R.success(I18nTranslate.$translate("erupt.exec_success"), buttonVo);
    }

    /**
     * Completion hints for a CODE_EDITOR field.
     *
     * @param eruptName Simple class name of the @Erupt model
     * @param field Field name with edit type CODE_EDITOR
     * @return hint words
     */
    @GetMapping("/code-edit-hints/{erupt}/{field}")
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT)
    public List<String> codeEditHints(@PathVariable("erupt") String eruptName,
                                      @PathVariable("field") String field) {
        EruptFieldModel fieldModel = EruptCoreService.getErupt(eruptName).getEruptFieldMap().get(field);
        CodeEditorType codeEditType = fieldModel.getEruptField().edit().codeEditType();
        List<String> hints = new java.util.ArrayList<>(Arrays.stream(codeEditType.hints()).toList());
        for (Class<? extends CodeEditHintHandler> handler : codeEditType.hintHandler()) {
            hints.addAll(EruptSpringUtil.getBean(handler).hint(codeEditType.hintParams()));
        }
        return hints;
    }

}
