package xyz.erupt.ai_canvas.service;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import xyz.erupt.ai.config.AiProp;
import xyz.erupt.ai.constants.SseEvent;
import xyz.erupt.ai.core.LlmCore;
import xyz.erupt.ai.core.LlmRequest;
import xyz.erupt.ai.model.LLM;
import xyz.erupt.ai.service.LLMService;
import xyz.erupt.ai.vo.SseBody;
import xyz.erupt.ai_canvas.fun.CanvasModelProvider;
import xyz.erupt.ai_canvas.model.AiCanvas;
import xyz.erupt.ai_canvas.model.AiCanvasVersion;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.service.EruptSessionService;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Builds the generation prompt (page skill + data source guide + model
 * structure + user message), sends it to the LLM and files the returned HTML
 * as a new version of the view.
 *
 * @author YuePeng
 * date 2026/8/3
 */
@Slf4j
@Service
public class AiCanvasService {

    private static final String SKILL_PATH = "/prompts/ai-canvas-skill.md";

    // Namespaced under prompts/ — a /style.json at the classpath root would clash
    // with the same-named resource shipped by other modules (e.g. erupt-cube-puzzle)
    private static final String STYLE_PATH = "/prompts/style.json";

    private static final String HTML_FENCE = "```html";

    private static String skillPrompt;

    private static List<CanvasStyle> styles;

    @Resource
    private LLMService llmService;

    @Resource
    private EruptDao eruptDao;

    @Resource
    private EruptSessionService eruptSessionService;

    @Resource
    private AiProp aiProp;

    private final Map<String, CanvasModelProvider> providers;

    public AiCanvasService(List<CanvasModelProvider> canvasModelProviders) {
        this.providers = canvasModelProviders.stream()
                .collect(Collectors.toMap(CanvasModelProvider::type, Function.identity()));
    }

    public Map<String, CanvasModelProvider> getProviders() {
        return providers;
    }

    public CanvasModelProvider provider(String type) {
        CanvasModelProvider provider = providers.get(type);
        if (null == provider) {
            throw new EruptWebApiRuntimeException("Unknown data source type: " + type);
        }
        return provider;
    }

    @Transactional
    public AiCanvasVersion generate(AiCanvas view, String message, String element) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(UserMessage.from(this.userMessage(this.draftHtml(view), message, element)));
        LLM llm = this.resolveLlm(view);
        String response = LlmCore.getLLM(llm).chat(this.llmRequest(view, llm), messages);
        return this.saveVersion(view, message, this.extractHtml(response));
    }

    private LLM resolveLlm(AiCanvas view) {
        LLM llm = null != view.getLlm() ? view.getLlm() : eruptDao.lambdaQuery(LLM.class)
                .eq(LLM::getDefaultLLM, true).eq(LLM::getEnable, true).limit(1).one();
        if (null == llm) throw new EruptWebApiRuntimeException("Not found LLM config");
        return llm;
    }

    // ReAct: the provider's verification tool is the ONLY tool of the round —
    // the global toolbox/MCP surface (autoCallTool) stays off during generation.
    // The canvas prompt rides in agentPrompt: a SystemMessage placed in the chat
    // context would be discarded by LlmCore's memory, which pins the system
    // message composed from the request prompts at index 0
    private LlmRequest llmRequest(AiCanvas view, LLM llm) {
        LlmRequest llmRequest = llm.toLlmRequest();
        llmRequest.setAutoCallTool(false);
        llmRequest.setAgentPrompt(this.buildSystem(view));
        Object verifyTool = this.provider(view.getDataType()).verifyTool();
        if (null != verifyTool) llmRequest.setTools(List.of(verifyTool));
        return llmRequest;
    }

    // Session key prefix of the explicit stop signal for a running generation
    private static final String STOP_KEY = "erupt-ai-canvas:generate-stop:";

    /**
     * Streaming variant: forwards tokens to the emitter as they arrive, then
     * files the version and answers DONE with either the version or an error.
     * Only an explicit stop discards the round — a mere disconnect (page
     * refresh, network hiccup) still persists the version, it shows on reload.
     */
    @Async
    public void generateSse(MetaContext metaContext, AiCanvas view, String message, String element, SseEmitter emitter) {
        try {
            MetaContext.set(metaContext);
            // Clear a stale stop signal left over from a previous round
            eruptSessionService.remove(STOP_KEY + view.getId());
            LLM llm = this.resolveLlm(view);
            LlmRequest llmRequest = this.llmRequest(view, llm);
            List<ChatMessage> context = new ArrayList<>();
            AtomicBoolean clientGone = new AtomicBoolean(false);
            emitter.onCompletion(() -> clientGone.set(true));
            emitter.onTimeout(() -> clientGone.set(true));
            emitter.onError(t -> clientGone.set(true));
            StringBuilder response = new StringBuilder();
            LlmCore.getLLM(llm).chatSse(llmRequest, this.userMessage(this.draftHtml(view), message, element), context, it -> {
                if (null != it.getThrowable()) {
                    if (!clientGone.get()) this.doneSse(emitter, null, it.getThrowable().getMessage());
                } else if (it.isFinish()) {
                    // Stopped by the user: discard the round, no version is filed
                    if (this.stopRequested(view.getId())) return;
                    try {
                        String text = null != it.getAiMessage() && null != it.getAiMessage().text()
                                ? it.getAiMessage().text() : response.toString();
                        AiCanvasVersion version = this.saveVersion(view, message, this.extractHtml(text));
                        if (!clientGone.get()) this.doneSse(emitter, version, null);
                    } catch (Exception e) {
                        if (!clientGone.get()) this.doneSse(emitter, null, e.getMessage());
                    }
                } else if (null != it.getCall()) {
                    // ReAct verification round: surface the tool name so the designer can show progress
                    if (!clientGone.get()) {
                        llmService.sendSseBody(emitter, new SseBody(SseEvent.CALL, it.getCall()));
                    }
                } else if (null != it.getCurrMessage() && !it.isThinking()) {
                    response.append(it.getCurrMessage());
                    if (!clientGone.get()) {
                        llmService.sendSseBody(emitter, new SseBody(SseEvent.TOKEN, it.getCurrMessage()));
                    }
                }
            });
        } catch (Exception e) {
            this.doneSse(emitter, null, e.getMessage());
        }
    }

    // Raise the stop signal; TTL-bounded to the longest a generation round can run
    public void stopGenerate(Long canvasId) {
        eruptSessionService.put(STOP_KEY + canvasId, "1", aiProp.getSseTimeout(), TimeUnit.MILLISECONDS);
    }

    private boolean stopRequested(Long canvasId) {
        return eruptSessionService.exist(STOP_KEY + canvasId);
    }

    // Single completion protocol: DONE carries either {version: {...}} or {error: "..."}
    private void doneSse(SseEmitter emitter, AiCanvasVersion version, String error) {
        Map<String, Object> payload;
        if (null != version) {
            Map<String, Object> versionMap = new HashMap<>();
            versionMap.put("id", version.getId());
            versionMap.put("version", version.getVersion());
            versionMap.put("message", version.getMessage());
            versionMap.put("style", version.getStyle());
            versionMap.put("createTime", String.valueOf(version.getCreateTime()));
            payload = Map.of("version", versionMap);
        } else {
            // The SSE path answers errors in-band instead of throwing, so log here —
            // otherwise a failed round leaves no server-side trace at all
            log.error("AI canvas generation failed: {}", error);
            payload = Map.of("error", null == error ? "Unknown error" : error);
        }
        llmService.sendSseBody(emitter, new SseBody(SseEvent.DONE, GsonFactory.getGson().toJson(payload)));
        llmService.completeSse(emitter);
    }

    // EruptDao calls carry their own transactions; called from both sync and SSE paths.
    // The new version becomes the working draft only — publishing stays explicit
    private AiCanvasVersion saveVersion(AiCanvas view, String message, String html) {
        Number max = (Number) eruptDao.lambdaQuery(AiCanvasVersion.class)
                .eq(AiCanvasVersion::getCanvasId, view.getId()).max(AiCanvasVersion::getVersion);
        AiCanvasVersion version = new AiCanvasVersion(view, null == max ? 1 : max.intValue() + 1, message, html);
        eruptDao.persistAndFlush(version);
        view.setActiveVersion(version.getId());
        eruptDao.mergeAndFlush(view);
        return version;
    }

    // Page source of the working draft (active version); null before the first generation
    public String draftHtml(AiCanvas view) {
        if (null == view.getActiveVersion()) return null;
        AiCanvasVersion version = eruptDao.find(AiCanvasVersion.class, view.getActiveVersion());
        return null == version ? null : version.getHtml();
    }

    // Page source served to viewers; null until the first explicit publish
    public String publishedHtml(AiCanvas view) {
        if (null == view.getPublishVersion()) return null;
        AiCanvasVersion version = eruptDao.find(AiCanvasVersion.class, view.getPublishVersion());
        return null == version ? null : version.getHtml();
    }

    // System prompt: page skill + optional style + data source guide + model structure.
    // No requirement history is carried: the current html is the single source of
    // truth for everything past rounds produced (including manual tweaks); replaying
    // old requirements risks resurrecting abandoned instructions.
    private String buildSystem(AiCanvas view) {
        if (StringUtils.isBlank(view.getDataType()) || StringUtils.isBlank(view.getTargetModel())) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("ai-canvas.model_not_configured"));
        }
        CanvasModelProvider provider = this.provider(view.getDataType());
        StringBuilder system = new StringBuilder(this.skill());
        this.styleOf(view.getStyle()).ifPresent(style -> system.append("\n\n").append(this.stylePrompt(style)));
        system.append("\n\n").append(provider.queryGuide());
        if (null != provider.verifyTool()) system.append("\n\n").append(VERIFY_PROMPT);
        system.append("\n\n# Data Model\n").append(provider.describe(view.getTargetModel()));
        return system.toString();
    }

    // ReAct contract shown to the LLM whenever the provider ships a verification tool
    private static final String VERIFY_PROMPT = """
            # Query Verification (ReAct)

            Tools are available in this conversation: metadata inspection tools plus a query verification tool. Every data query the page will run MUST be proven to work before it appears in the final document:

            1. Plan the data queries the page needs; use the inspection tools first whenever a structure, field code or option list you need is not already in the prompt — never guess it.
            2. Call the verification tool once per planned query, passing EXACTLY the same JSON argument the page will pass to the SDK function.
            3. If the call fails or the rows don't look as expected, fix the query (field codes, conditions, parameters) and verify again.
            4. Only verified queries may be embedded, and the page may only read row keys actually observed in the verified results.

            After all queries pass, output the complete HTML document as instructed. Never skip verification, and do not describe the tool calls in the final answer.""";

    // Switch the working draft; viewers keep seeing the published version.
    // The style snapshot is restored so the next generation round stays
    // consistent with the page being iterated on
    @Transactional
    public void activate(AiCanvas view, AiCanvasVersion version) {
        view.setActiveVersion(version.getId());
        view.setStyle(version.getStyle());
        eruptDao.merge(view);
    }

    // Point viewers at the working draft; versions are immutable so no copy is needed
    @Transactional
    public void publish(AiCanvas view) {
        if (null == view.getActiveVersion()) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("ai-canvas.not_generated"));
        }
        view.setPublishVersion(view.getActiveVersion());
        eruptDao.merge(view);
    }

    private String userMessage(String draftHtml, String message, String element) {
        StringBuilder user = new StringBuilder(message);
        // The user picked a concrete element on the preview: scope the change to it.
        // Framed as an explicit instruction so it is not drowned out by the
        // "rewrite the whole page" directive that follows. Only the selector is sent —
        // the element lives in the page source below, so its markup need not be shipped.
        if (StringUtils.isNotBlank(element)) {
            user.append("\n\n# Target Element\nThe requirement above refers to a specific element the user selected on the page, identified by the CSS selector `")
                    .append(element).append("`. Locate this element in the current page source below and apply the change there; leave the rest of the page untouched unless the requirement clearly implies wider edits. The selector is derived from the rendered DOM, so match by structure if it does not resolve verbatim — e.g. browsers insert an implicit <tbody> that the source may omit.");
        }
        if (StringUtils.isNotBlank(draftHtml)) {
            user.append("\n\n# Current Page Source\nRevise the page below against the requirement above and output the full document again.\n")
                    .append(HTML_FENCE).append("\n").append(draftHtml).append("\n```");
        }
        return user.toString();
    }

    private String extractHtml(String response) {
        if (StringUtils.isBlank(response)) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("ai-canvas.empty_response"));
        }
        String html = findHtmlDocument(response);
        if (null == html) {
            // Full raw response, the only material to diagnose why extraction failed
            // (truncated output, refusal, wrong format...)
            log.error("No HTML document found in the AI response:\n{}", response);
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("ai-canvas.bad_response"));
        }
        return html;
    }

    // Pure extraction: ```html fence first, then a bare document; null when neither is found
    static String findHtmlDocument(String response) {
        int fence = response.indexOf(HTML_FENCE);
        if (fence >= 0) {
            int contentStart = fence + HTML_FENCE.length();
            int end = response.lastIndexOf("```");
            if (end > contentStart) return response.substring(contentStart, end).trim();
        }
        int docStart = response.indexOf("<!DOCTYPE");
        if (docStart < 0) docStart = response.indexOf("<html");
        int docEnd = response.lastIndexOf("</html>");
        if (docStart >= 0 && docEnd > docStart) {
            return response.substring(docStart, docEnd + "</html>".length());
        }
        return null;
    }

    @SneakyThrows
    private String skill() {
        if (null == skillPrompt) {
            try (InputStream is = AiCanvasService.class.getResourceAsStream(SKILL_PATH)) {
                skillPrompt = new String(Objects.requireNonNull(is, SKILL_PATH).readAllBytes(), StandardCharsets.UTF_8);
            }
        }
        return skillPrompt;
    }

    @SneakyThrows
    public List<CanvasStyle> getStyles() {
        if (null == styles) {
            try (InputStream is = AiCanvasService.class.getResourceAsStream(STYLE_PATH)) {
                String json = new String(Objects.requireNonNull(is, STYLE_PATH).readAllBytes(), StandardCharsets.UTF_8);
                StyleFile styleFile = GsonFactory.getGson().fromJson(json, StyleFile.class);
                styles = styleFile.getData();
            }
        }
        return styles;
    }

    private Optional<CanvasStyle> styleOf(String id) {
        if (StringUtils.isBlank(id)) return Optional.empty();
        return this.getStyles().stream().filter(it -> it.getId().equals(id)).findFirst();
    }

    // Style section: design-language facts plus the style's demo page as a visual
    // reference — assets and data access still follow the skill sections
    private String stylePrompt(CanvasStyle style) {
        StringBuilder sb = new StringBuilder("## Page Style: ").append(style.getName()).append("\n");
        if (StringUtils.isNotBlank(style.getDescription())) {
            sb.append(style.getDescription()).append("\n");
        }
        if (null != style.getSystem()) {
            CanvasStyle.StyleSystem system = style.getSystem();
            if (StringUtils.isNotBlank(system.getMode())) sb.append("- Mode: ").append(system.getMode()).append("\n");
            if (null != system.getChartPalette() && !system.getChartPalette().isEmpty()) {
                sb.append("- Chart palette: ").append(String.join(", ", system.getChartPalette())).append("\n");
            }
            if (StringUtils.isNotBlank(system.getChartLibrary())) {
                sb.append("- Chart library: ").append(system.getChartLibrary()).append("\n");
            }
        }
        if (StringUtils.isNotBlank(style.getDemoHtml())) {
            sb.append("\nReference page in this style — copy its visual language (layout, colors, typography, component shapes), ")
                    .append("NOT its content, assets or data code; those must follow the sections above:\n")
                    .append(HTML_FENCE).append("\n").append(style.getDemoHtml()).append("\n```");
        }
        return sb.toString();
    }

    @Getter
    @Setter
    public static class StyleFile {
        private List<CanvasStyle> data;
    }

    @Getter
    @Setter
    public static class CanvasStyle {
        private String id;
        private String name;
        private String description;
        private Boolean isOfficial;
        private StyleSystem system;
        private String demoHtml;

        @Getter
        @Setter
        public static class StyleSystem {
            private String mode;
            private List<String> chartPalette;
            private String chartLibrary;
        }
    }

}
