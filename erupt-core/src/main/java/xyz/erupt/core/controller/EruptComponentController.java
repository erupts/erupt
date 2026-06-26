package xyz.erupt.core.controller;

import com.google.gson.JsonObject;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.annotation.fun.CodeEditHintHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.annotation.sub_field.sub_edit.AutoCompleteType;
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.exception.EruptApiErrorTip;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.R;

import java.util.Arrays;
import java.util.List;

/**
 * @author YuePeng
 * date 2020-08-14
 */
@RestController
@RequestMapping(EruptRestPath.ERUPT_COMP)
public class EruptComponentController {

    /**
     * autoComplete component Interconnection Interface
     *
     * @param field    Auto-fill component fields
     * @param val      The value of the input box
     * @param data     Erupt form object
     * @return Association results
     */
    @PostMapping("/auto-complete/{erupt}/{field}")
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT)
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
        try {
            return EruptSpringUtil.getBean(autoCompleteType.handler()).completeHandler(o, val, autoCompleteType.param());
        } catch (Exception e) {
            throw new EruptApiErrorTip(e.getMessage(), R.PromptWay.MESSAGE);
        }
    }

    //Gets the CHOICE component drop-down list
    @GetMapping("/choice-item/{erupt}/{field}")
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT)
    public List<VLModel> choiceItem(@PathVariable("erupt") String eruptName,
                                    @PathVariable("field") String field) {
        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);
        EruptFieldModel fieldModel = eruptModel.getEruptFieldMap().get(field);
        return EruptUtil.getChoiceList(eruptModel, fieldModel.getEruptField().edit());
    }

    @PostMapping("/choice-item-filter/{erupt}/{field}")
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT)
    public List<VLModel> choiceFilter(@PathVariable("erupt") String eruptName,
                                      @PathVariable("field") String field,
                                      @RequestBody JsonObject data) {
        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);
        EruptFieldModel fieldModel = eruptModel.getEruptFieldMap().get(field);
        Object o = GsonFactory.getGson().fromJson(data.toString(), eruptModel.getClazz());
        return EruptUtil.getChoiceListFilter(eruptModel, fieldModel.getEruptField().edit(), o);
    }

    //Gets the TAGS component data
    @PostMapping("/tags-item/{erupt}/{field}")
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT)
    public List<String> tagsItem(@PathVariable("erupt") String eruptName,
                                 @PathVariable("field") String field,
                                 @RequestBody JsonObject data
    ) {
        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);
        EruptFieldModel fieldModel = eruptModel.getEruptFieldMap().get(field);
        Object o = GsonFactory.getGson().fromJson(data.toString(), eruptModel.getClazz());
        return EruptUtil.getTagList(fieldModel.getEruptField().edit().tagsType(), o);
    }

    //Gets the CodeEdit component hint data
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
