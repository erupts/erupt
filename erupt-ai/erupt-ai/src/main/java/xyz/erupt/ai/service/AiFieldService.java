package xyz.erupt.ai.service;

import com.google.gson.JsonObject;
import dev.langchain4j.model.output.TokenUsage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import xyz.erupt.ai.constants.SseEvent;
import xyz.erupt.ai.core.LlmCore;
import xyz.erupt.ai.core.LlmRequest;
import xyz.erupt.ai.model.LLM;
import xyz.erupt.ai.vo.AiFieldRequest;
import xyz.erupt.ai.vo.SseBody;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Drafts the value of a single form field. The model is told what the record is, what the
 * field means, and what the user has already filled in elsewhere on the same form.
 *
 * @author YuePeng
 */
@Slf4j
@Service
public class AiFieldService {

    @Resource
    private LLMService llmService;

    // Never handed to the model: a secret has no business leaving the form, and a binary
    // or opaque value is noise that only eats context
    private static final Set<EditType> EXCLUDED_TYPES = EnumSet.of(
            EditType.PASSWORD, EditType.ATTACHMENT, EditType.SIGNATURE, EditType.MAP, EditType.TPL,
            EditType.BUTTON, EditType.EMPTY, EditType.DIVIDE, EditType.CALLOUT);

    // A sibling value longer than this is cut down: the brief needs the gist, not the essay
    private static final int MAX_SIBLING_LENGTH = 300;

    // Max error text kept on the audit line; upstream messages can run long and the full
    // stack trace is already logged separately
    private static final int MAX_ERROR_LENGTH = 200;

    /** What an audit line needs to identify the call, without carrying any of its content */
    public record FieldRef(String eruptName, String fieldName, AiFieldRequest.Action action) {
    }

    // Output shape the model must respect, keyed by the component that will hold the text
    private static String formatRule(EditType editType) {
        return switch (editType) {
            case CODE_EDITOR -> "Output raw source code only. No markdown fences, no prose, no explanation.";
            case MARKDOWN -> "Output markdown. Do not wrap the whole answer in a code fence.";
            case HTML_EDITOR -> "Output an HTML fragment (no <html>, <head> or <body> wrapper), suitable for a rich text editor.";
            case INPUT -> "Output a single line of plain text, with no quotes, no trailing period and no explanation.";
            default -> "Output plain text only, with no markdown formatting and no explanation.";
        };
    }

    /** Reads the component type off the built field json; unknown names simply carry no rule */
    public static EditType editTypeOf(JsonObject edit) {
        String name = asString(edit, "type");
        if (StringUtils.isBlank(name)) return null;
        try {
            return EditType.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static String actionRule(AiFieldRequest.Action action) {
        return switch (action) {
            case GENERATE -> "Write this field from scratch, based on the record described below.";
            case POLISH -> "Rewrite the given text so it reads better. Keep its meaning and its language.";
            case CONTINUE -> "Continue the given text from exactly where it stops. Output only the continuation, not a repeat of what is already there.";
            case SHORTEN -> "Say the same thing in noticeably fewer words. Keep every fact.";
            case EXPAND -> "Say the same thing in more detail, staying faithful to what is already there.";
            case CUSTOM -> "Apply the user's instruction to this field.";
        };
    }

    public String buildSystemPrompt(EruptModel eruptModel, EruptFieldModel fieldModel, EditType editType,
                                    AiFieldRequest.Action action) {
        JsonObject edit = fieldModel.getEruptFieldJson().getAsJsonObject("edit");
        StringBuilder sb = new StringBuilder();
        sb.append("You fill in one field of a data entry form in an admin system. ")
                .append("You produce the field's value and nothing else — never a preamble, never a sign-off, ")
                .append("never a comment about what you did.\n\n");
        sb.append("Record type: ").append(eruptModel.getErupt().name());
        if (StringUtils.isNotBlank(eruptModel.getErupt().desc())) {
            sb.append(" — ").append(eruptModel.getErupt().desc());
        }
        sb.append("\nField to write: ").append(asString(edit, "title"));
        String desc = asString(edit, "desc");
        if (StringUtils.isNotBlank(desc)) sb.append(" — ").append(desc);
        sb.append("\n");
        // Field-level authoring guidance from @Edit(prompt = "..."), the model owner's own words.
        // A remote (erupt-cloud) field carries no local annotation, so there is nothing to read.
        String fieldPrompt = null == fieldModel.getEruptField() ? null : fieldModel.getEruptField().edit().prompt();
        if (StringUtils.isNotBlank(fieldPrompt)) {
            sb.append("\nGuidance for this field:\n").append(fieldPrompt).append("\n");
        }
        Integer maxLength = maxLength(edit, editType);
        if (null != maxLength && maxLength > 0) {
            sb.append("\nHard limit: the value must not exceed ").append(maxLength).append(" characters.");
        }
        sb.append("\n\n").append(actionRule(action));
        sb.append("\n").append(formatRule(editType));
        sb.append("\nAnswer in the same language the rest of the form is written in.");
        return sb.toString();
    }

    public String buildUserPrompt(EruptModel eruptModel, String targetField, AiFieldRequest request) {
        StringBuilder sb = new StringBuilder();
        String siblings = this.siblingContext(eruptModel, targetField, request.getForm());
        if (StringUtils.isNotBlank(siblings)) {
            sb.append("What the rest of this record already says:\n").append(siblings).append("\n\n");
        }
        if (StringUtils.isNotBlank(request.getSelection())) {
            sb.append("The user highlighted this part of the field, and only this part is to be rewritten:\n")
                    .append("<<<\n").append(request.getSelection()).append("\n>>>\n\n");
            if (StringUtils.isNotBlank(request.getCurrent())) {
                sb.append("It sits inside the full field value, given here for context only:\n")
                        .append("<<<\n").append(request.getCurrent()).append("\n>>>\n\n");
            }
        } else if (StringUtils.isNotBlank(request.getCurrent())) {
            sb.append("Current value of the field:\n<<<\n").append(request.getCurrent()).append("\n>>>\n\n");
        }
        if (StringUtils.isNotBlank(request.getInstruction())) {
            sb.append("The user asks: ").append(request.getInstruction());
        } else {
            sb.append("Write the field now.");
        }
        return sb.toString();
    }

    // Labelled values of the other fields on the form, so the model drafts with the record in view
    private String siblingContext(EruptModel eruptModel, String targetField, Map<String, Object> form) {
        if (null == form || form.isEmpty()) return null;
        StringBuilder sb = new StringBuilder();
        for (EruptFieldModel fieldModel : eruptModel.getEruptFieldModels()) {
            String name = fieldModel.getFieldName();
            if (name.equals(targetField) || !form.containsKey(name)) continue;
            JsonObject edit = fieldModel.getEruptFieldJson().getAsJsonObject("edit");
            if (null == edit) continue;
            if (EXCLUDED_TYPES.contains(editTypeOf(edit))) continue;
            String value = stringify(form.get(name));
            if (StringUtils.isBlank(value)) continue;
            if (value.length() > MAX_SIBLING_LENGTH) {
                value = value.substring(0, MAX_SIBLING_LENGTH) + "…";
            }
            String title = asString(edit, "title");
            sb.append("- ").append(StringUtils.isBlank(title) ? name : title).append(": ").append(value).append("\n");
        }
        return sb.toString();
    }

    private static String stringify(Object value) {
        if (null == value) return null;
        // A reference field arrives as its picked object; its label is the only part worth sending
        if (value instanceof Map<?, ?> map) {
            Object label = map.get("label");
            return null == label ? null : label.toString();
        }
        return value.toString().trim();
    }

    private static String asString(JsonObject json, String key) {
        return null != json && json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : "";
    }

    // Length cap the component itself enforces, so the model is not asked for text the form will reject
    private static Integer maxLength(JsonObject edit, EditType editType) {
        JsonObject type = switch (editType) {
            case INPUT -> edit.getAsJsonObject("inputType");
            case TEXTAREA -> edit.getAsJsonObject("textareaType");
            default -> null;
        };
        if (null == type || !type.has("length") || type.get("length").isJsonNull()) return null;
        return type.get("length").getAsInt();
    }

    /**
     * Streams the drafted value straight to the browser. Nothing is persisted: the value only
     * becomes real if the user keeps it and saves the form.
     * <p>
     * Every call leaves one audit line — who, which field, which model, how many tokens, how
     * long. Deliberately no prompt and no generated text: those are business data, and a log
     * file is the wrong place to keep them.
     */
    @Async
    public void stream(MetaContext metaContext, SseEmitter emitter, LLM llmModel, FieldRef ref,
                       String systemPrompt, String userPrompt) {
        long startedAt = System.currentTimeMillis();
        try {
            MetaContext.set(metaContext);
            LlmRequest llmRequest = llmModel.toLlmRequest();
            // No toolbox, no MCP, no provider prompts: this call writes text, it does not act
            llmRequest.setAgentPrompt(systemPrompt);
            AtomicBoolean closed = new AtomicBoolean(false);
            emitter.onCompletion(() -> closed.set(true));
            emitter.onTimeout(() -> closed.set(true));
            emitter.onError(t -> closed.set(true));
            LlmCore.getLLM(llmModel.getLlm()).chatSse(llmRequest, userPrompt, new ArrayList<>(), it -> {
                if (closed.get()) return;
                if (null != it.getThrowable()) {
                    this.audit(ref, llmModel, null, startedAt, it.getThrowable().getMessage());
                    llmService.sendSseBody(emitter, new SseBody(SseEvent.TOKEN, it.getThrowable().getMessage()));
                    llmService.sendSseDone(emitter);
                    llmService.completeSse(emitter);
                } else if (it.isFinish()) {
                    this.audit(ref, llmModel, it.getUsage(), startedAt, null);
                    llmService.sendSseDone(emitter);
                    llmService.completeSse(emitter);
                } else if (null != it.getCurrMessage() && !it.isThinking()) {
                    llmService.sendSseBody(emitter, new SseBody(SseEvent.TOKEN, it.getCurrMessage()));
                }
            });
        } catch (Exception e) {
            this.audit(ref, llmModel, null, startedAt, e.getMessage());
            log.error("AI field generation failed: {}", e.getMessage(), e);
            llmService.sendSseBody(emitter, new SseBody(SseEvent.TOKEN, e.getMessage()));
            llmService.sendSseDone(emitter);
            llmService.completeSse(emitter);
        }
    }

    /**
     * One line per call, the same key set whether it succeeded or not, so a log collector
     * needs a single rule to parse both.
     */
    private void audit(FieldRef ref, LLM llmModel, TokenUsage usage, long startedAt, String error) {
        try {
            Object uid = null == MetaContext.getUser() ? null : MetaContext.getUser().getUid();
            long ms = System.currentTimeMillis() - startedAt;
            if (null == error) {
                log.info("AI field draft | ok=true uid={} erupt={} field={} action={} llm={} model={} in={} out={} ms={}",
                        uid, ref.eruptName(), ref.fieldName(), ref.action(), llmModel.getLlm(), llmModel.getModel(),
                        null == usage ? 0 : usage.inputTokenCount(), null == usage ? 0 : usage.outputTokenCount(), ms);
            } else {
                log.warn("AI field draft | ok=false uid={} erupt={} field={} action={} llm={} model={} in=0 out=0 ms={} err=\"{}\"",
                        uid, ref.eruptName(), ref.fieldName(), ref.action(), llmModel.getLlm(), llmModel.getModel(),
                        ms, StringUtils.abbreviate(error, MAX_ERROR_LENGTH));
            }
        } catch (Exception e) {
            // auditing must never break the feature it is auditing
            log.debug("AI field audit failed: {}", e.getMessage());
        }
    }

}
