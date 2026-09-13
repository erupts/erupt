package xyz.erupt.ai.controller;

import com.google.gson.JsonObject;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import xyz.erupt.ai.config.AiProp;
import xyz.erupt.ai.model.LLM;
import xyz.erupt.ai.service.AiFieldService;
import xyz.erupt.ai.service.LLMService;
import xyz.erupt.ai.vo.AiFieldRequest;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.jpa.dao.EruptDao;

import java.util.EnumSet;
import java.util.Set;

/**
 * Inline AI writing assistant for a single form field.
 *
 * @author YuePeng
 */
@Slf4j
@RestController
@RequestMapping(EruptRestPath.ERUPT_API + "/ai/field")
public class AiFieldController {

    @Resource
    private EruptDao eruptDao;

    @Resource
    private AiProp aiProp;

    @Resource
    private LLMService llmService;

    @Resource
    private AiFieldService aiFieldService;

    // Components that hold free text worth drafting; mirrors the @Match on Edit#ai
    private static final Set<EditType> AI_TYPES = EnumSet.of(
            EditType.INPUT, EditType.TEXTAREA, EditType.HTML_EDITOR, EditType.CODE_EDITOR, EditType.MARKDOWN);

    /**
     * Drafts one field and streams it back. Menu permission and the parent/child nesting claim are
     * verified by {@link EruptRouter}; what is left to check here is that this particular field
     * actually opted in, and that the caller may write the record at all.
     */
    @PostMapping(value = "/{erupt}/{fieldName}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT)
    public SseEmitter generate(@PathVariable("erupt") String eruptName,
                               @PathVariable("fieldName") String fieldName,
                               @RequestBody AiFieldRequest request) {
        SseEmitter emitter = new SseEmitter(aiProp.getSseTimeout());
        EruptModel eruptModel = EruptCoreService.getEruptView(eruptName);
        Erupts.requireTrue(null != eruptModel, I18nTranslate.$translate("Model not found"));
        // Drafting a value is a write-side affordance: only someone who could type it may ask for it
        Erupts.powerLegal(eruptModel, power -> power.isAdd() || power.isEdit());
        // @Power(ai = false) bars AI from the model entirely, and this hands it the whole form
        Erupts.requireTrue(null == eruptModel.getErupt() || eruptModel.getErupt().power().ai(),
                I18nTranslate.$translate("AI is disabled for this model"));

        EruptFieldModel fieldModel = eruptModel.getEruptFieldModels().stream()
                .filter(it -> it.getFieldName().equals(fieldName)).findFirst().orElse(null);
        Erupts.requireTrue(null != fieldModel, I18nTranslate.$translate("Field not found"));
        JsonObject edit = fieldModel.getEruptFieldJson().getAsJsonObject("edit");
        Erupts.requireTrue(null != edit, I18nTranslate.$translate("Field not found"));
        EditType editType = AiFieldService.editTypeOf(edit);
        Erupts.requireTrue(AI_TYPES.contains(editType),
                I18nTranslate.$translate("The AI assistant is not available on this field type"));
        // The field can turn the assistant off; absent means on, matching the annotation default
        Erupts.requireTrue(!edit.has("ai") || edit.get("ai").getAsBoolean(),
                I18nTranslate.$translate("The AI assistant is disabled for this field"));

        LLM llmModel = eruptDao.lambdaQuery(LLM.class)
                .eq(LLM::getDefaultLLM, true).eq(LLM::getEnable, true).limit(1).one();
        if (null == llmModel) {
            llmService.sendSseDone(emitter);
            llmService.completeSse(emitter);
            return emitter;
        }
        eruptDao.detach(llmModel);
        aiFieldService.stream(MetaContext.get(), emitter, llmModel,
                new AiFieldService.FieldRef(eruptName, fieldName, request.getAction()),
                aiFieldService.buildSystemPrompt(eruptModel, fieldModel, editType, request.getAction()),
                aiFieldService.buildUserPrompt(eruptModel, fieldName, request));
        return emitter;
    }

}
