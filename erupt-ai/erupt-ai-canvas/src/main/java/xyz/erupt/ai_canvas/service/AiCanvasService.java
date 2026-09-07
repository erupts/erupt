package xyz.erupt.ai_canvas.service;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.output.FinishReason;
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
import xyz.erupt.ai_canvas.model.AiCanvasModel;
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
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Builds the generation prompt (page skill + one data source guide per bound
 * provider, plus its write guide when a binding allows writes + the structure
 * and allowed writes of every bound model + user message), sends it to
 * the LLM and files the returned HTML as a new version of the view.
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
        LlmRequest llmRequest = this.llmRequest(view, llm);
        this.markGenerating(view.getId(), message);
        try {
            String response = LlmCore.getLLM(llm).chat(llmRequest, messages);
            return this.finishRound(view, message, llm, llmRequest, messages, response, false, null);
        } finally {
            this.clearGenerating(view.getId());
        }
    }

    private LLM resolveLlm(AiCanvas view) {
        LLM llm = null != view.getLlm() ? view.getLlm() : eruptDao.lambdaQuery(LLM.class)
                .eq(LLM::getDefaultLLM, true).eq(LLM::getEnable, true).limit(1).one();
        if (null == llm) throw new EruptWebApiRuntimeException("Not found LLM config");
        return llm;
    }

    // ReAct: the verification tools of the bound providers are the ONLY tools of
    // the round — the global toolbox/MCP surface (autoCallTool) stays off during generation.
    // The canvas prompt rides in agentPrompt: a SystemMessage placed in the chat
    // context would be discarded by LlmCore's memory, which pins the system
    // message composed from the request prompts at index 0
    private LlmRequest llmRequest(AiCanvas view, LLM llm) {
        LlmRequest llmRequest = llm.toLlmRequest();
        llmRequest.setAutoCallTool(false);
        llmRequest.setAgentPrompt(this.buildSystem(view));
        Map<String, List<AiCanvasModel>> byType = groupByType(orderedBindings(view));
        List<Object> verifyTools = byType.keySet().stream()
                .map(type -> this.provider(type).verifyTool(byType.get(type))).filter(Objects::nonNull).toList();
        if (!verifyTools.isEmpty()) llmRequest.setTools(verifyTools);
        return llmRequest;
    }

    // Session key prefix of the explicit stop signal for a running generation
    private static final String STOP_KEY = "erupt-ai-canvas:generate-stop:";

    // Session key prefix of the "a round is running" marker read by the designer
    private static final String RUNNING_KEY = "erupt-ai-canvas:generate-running:";

    /**
     * Lifetime of the running marker. It is deliberately far shorter than the SSE
     * timeout and kept alive by a heartbeat on every stream event: when the
     * container is killed mid-round the heartbeat stops with it, so the marker
     * expires on its own instead of pinning the designer to a round that no longer
     * exists. Long enough to span the model's quiet stretches, such as the wait
     * before the first token of a tool-heavy round.
     */
    static final long RUNNING_TTL_MS = 2 * 60 * 1000L;

    // Heartbeats ride on the token stream, so throttle them well below the TTL
    static final long HEARTBEAT_INTERVAL_MS = 20 * 1000L;

    /**
     * What the designer shows for a round already in flight when the page is opened.
     * The two timestamps are deliberately separate: startedAt is preserved across
     * heartbeats and drives the elapsed time on screen, while beatAt moves with every
     * heartbeat and is the only thing that says whether the round is still alive.
     * A marker written before this field existed deserializes with beatAt 0 and is
     * therefore read as dead, which is what we want for a previous deployment's leftovers.
     */
    @Getter
    @Setter
    public static class GeneratingState {
        private long startedAt;
        private long beatAt;
        private String message;

        public GeneratingState() {
        }

        public GeneratingState(String message) {
            this.startedAt = System.currentTimeMillis();
            this.beatAt = this.startedAt;
            this.message = message;
        }
    }

    private void markGenerating(Long canvasId, String message) {
        // Preserve the original start time so the designer can show how long it has run;
        // the constructor already stamped beatAt with now, which is what keeps it alive
        GeneratingState current = this.generatingState(canvasId);
        GeneratingState state = new GeneratingState(message);
        if (null != current) state.setStartedAt(current.getStartedAt());
        eruptSessionService.put(RUNNING_KEY + canvasId,
                GsonFactory.getGson().toJson(state), RUNNING_TTL_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * Keep the marker alive while the stream is producing. Re-puts the value rather
     * than calling expire(), which throws on a key that is already gone in local
     * session mode. A round the user stopped is not revived.
     */
    private void heartbeatGenerating(Long canvasId, String message) {
        if (this.stopRequested(canvasId)) return;
        this.markGenerating(canvasId, message);
    }

    public void clearGenerating(Long canvasId) {
        eruptSessionService.remove(RUNNING_KEY + canvasId);
    }

    /**
     * The round the designer should show as running, or null when there is none.
     * Two things retire a marker. The session entry carries its own TTL, renewed by
     * every heartbeat, so on both backends a container killed mid-round simply stops
     * renewing it. A Redis-backed session outlives the JVM, so the age of the last
     * heartbeat is checked as well and a lapsed marker is dropped rather than re-read
     * on every poll. Liveness is never judged by startedAt, which is held constant
     * across heartbeats: that would report every round older than the TTL as finished
     * while it is still streaming.
     */
    public GeneratingState generatingState(Long canvasId) {
        Object raw = eruptSessionService.get(RUNNING_KEY + canvasId);
        if (null == raw || StringUtils.isBlank(raw.toString())) return null;
        GeneratingState state = GsonFactory.getGson().fromJson(raw.toString(), GeneratingState.class);
        if (!markerAlive(state, System.currentTimeMillis())) {
            this.clearGenerating(canvasId);
            return null;
        }
        return state;
    }

    // A round is alive while its last heartbeat is younger than the marker lifetime
    static boolean markerAlive(GeneratingState state, long now) {
        return null != state && now - state.getBeatAt() <= RUNNING_TTL_MS;
    }

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
            // The marker is what the designer reads after a refresh; the stream itself
            // is gone by then. Cleared on every terminal path below, and self-expiring
            // when the container dies mid-round
            this.markGenerating(view.getId(), message);
            AtomicLong lastBeat = new AtomicLong(System.currentTimeMillis());
            LlmCore.getLLM(llm).chatSse(llmRequest, this.userMessage(this.draftHtml(view), message, element), context, it -> {
                if (null != it.getThrowable()) {
                    this.clearGenerating(view.getId());
                    if (!clientGone.get()) this.doneSse(emitter, null, it.getThrowable().getMessage());
                } else if (it.isFinish()) {
                    // Stopped by the user: discard the round, no version is filed
                    if (this.stopRequested(view.getId())) {
                        this.clearGenerating(view.getId());
                        return;
                    }
                    try {
                        String text = null != it.getAiMessage() && null != it.getAiMessage().text()
                                ? it.getAiMessage().text() : response.toString();
                        boolean cutOff = FinishReason.LENGTH == it.getFinishReason();
                        AiCanvasVersion version = this.finishRound(view, message, llm, llmRequest, context, text, cutOff, step -> {
                            // Validation and the repair round run here, after the last token:
                            // renew the marker so a refresh still shows the round as running
                            this.heartbeatGenerating(view.getId(), message);
                            if (!clientGone.get()) llmService.sendSseBody(emitter, new SseBody(SseEvent.CALL, step));
                        });
                        if (!clientGone.get()) this.doneSse(emitter, version, null);
                    } catch (Exception e) {
                        if (!clientGone.get()) this.doneSse(emitter, null, e.getMessage());
                    } finally {
                        // Only here is the round genuinely over, repair round included
                        this.clearGenerating(view.getId());
                    }
                } else if (null != it.getCall()) {
                    this.beat(view.getId(), message, lastBeat);
                    // ReAct verification round: surface the tool name so the designer can show progress
                    if (!clientGone.get()) {
                        llmService.sendSseBody(emitter, new SseBody(SseEvent.CALL, it.getCall()));
                    }
                } else if (null != it.getCurrMessage()) {
                    // Thinking tokens keep the round alive too, they just are not page source
                    this.beat(view.getId(), message, lastBeat);
                    if (it.isThinking()) return;
                    response.append(it.getCurrMessage());
                    if (!clientGone.get()) {
                        llmService.sendSseBody(emitter, new SseBody(SseEvent.TOKEN, it.getCurrMessage()));
                    }
                }
            });
        } catch (Exception e) {
            this.clearGenerating(view.getId());
            this.doneSse(emitter, null, e.getMessage());
        }
    }

    // Tokens arrive far too fast to touch the session store on each one
    static boolean beatDue(long now, long lastBeat) {
        return now - lastBeat >= HEARTBEAT_INTERVAL_MS;
    }

    private void beat(Long canvasId, String message, AtomicLong lastBeat) {
        long now = System.currentTimeMillis();
        if (!beatDue(now, lastBeat.get())) return;
        lastBeat.set(now);
        this.heartbeatGenerating(canvasId, message);
    }

    // Raise the stop signal; TTL-bounded to the longest a generation round can run
    public void stopGenerate(Long canvasId) {
        eruptSessionService.put(STOP_KEY + canvasId, "1", aiProp.getSseTimeout(), TimeUnit.MILLISECONDS);
        // Always the designer's way out: a round orphaned by a restart still clears here
        this.clearGenerating(canvasId);
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

    // Progress label of the repair round, surfaced to the designer like a tool call
    public static final String REPAIR_STEP = "repairHtml";

    /**
     * Turn the model's answer into a version: extract the document, validate it,
     * and when it cannot render give the model ONE repair round with the findings
     * before filing. A second failure is reported to the user instead of saving a
     * page that would show up blank. {@code cutOff} flags an answer the model
     * truncated at its output limit — the document may still look complete, but
     * the model must be told to tighten it.
     */
    AiCanvasVersion finishRound(AiCanvas view, String message, LLM llm, LlmRequest llmRequest, List<ChatMessage> context,
                                String response, boolean cutOff, Consumer<String> progress) {
        List<String> problems = problemsOf(response, cutOff);
        if (!problems.isEmpty()) {
            log.warn("AI canvas page failed validation, asking the model to repair it: {}", problems);
            if (null != progress) progress.accept(REPAIR_STEP);
            context.add(UserMessage.from(repairMessage(problems)));
            response = LlmCore.getLLM(llm).chat(llmRequest, context);
            problems = problemsOf(response, false);
            if (!problems.isEmpty()) {
                log.error("AI canvas page still invalid after the repair round: {}\n{}", problems, response);
                throw new EruptWebApiRuntimeException(I18nTranslate.$translate("ai-canvas.invalid_html") + ": " + String.join("; ", problems));
            }
        }
        return this.saveVersion(view, message, findHtmlDocument(response));
    }

    // Everything that stops the answer from becoming a renderable page, phrased for the model
    static List<String> problemsOf(String response, boolean cutOff) {
        List<String> problems = new ArrayList<>();
        if (StringUtils.isBlank(response)) {
            problems.add("the answer was empty");
            return problems;
        }
        if (cutOff) problems.add("the answer was cut off at the model's output limit; the page must be more compact (less CSS, no comments, no unused code)");
        String html = findHtmlDocument(response);
        if (null == html) {
            problems.add("no complete HTML document was found (a ```html block ending with </html>)");
            return problems;
        }
        problems.addAll(CanvasHtmlValidator.validate(html));
        return problems;
    }

    static String repairMessage(List<String> problems) {
        StringBuilder sb = new StringBuilder("# Validation Failed\nThe document you produced cannot be rendered. Problems found:\n");
        problems.forEach(p -> sb.append("- ").append(p).append("\n"));
        return sb.append("\nFix every problem and output the COMPLETE corrected HTML document again in a single ```html block, no explanations. ")
                .append("Keep the page otherwise identical. If the output was cut off, shorten it (fewer styles and comments) so the whole document fits.")
                .toString();
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

    // System prompt: page skill + optional style + one query guide per bound data
    // source type + the structure of every bound model. No requirement history is
    // carried: the current html is the single source of truth for everything past
    // rounds produced (including manual tweaks); replaying old requirements risks
    // resurrecting abandoned instructions.
    private String buildSystem(AiCanvas view) {
        List<AiCanvasModel> bindings = orderedBindings(view);
        if (bindings.isEmpty() || bindings.stream().anyMatch(it ->
                StringUtils.isBlank(it.getDataType()) || StringUtils.isBlank(it.getModel()))) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("ai-canvas.model_not_configured"));
        }
        Map<String, List<AiCanvasModel>> byType = groupByType(bindings);
        StringBuilder system = new StringBuilder(this.skill());
        this.styleOf(view.getStyle()).ifPresent(style -> system.append("\n\n").append(this.stylePrompt(style)));
        boolean verify = false;
        for (Map.Entry<String, List<AiCanvasModel>> entry : byType.entrySet()) {
            CanvasModelProvider provider = this.provider(entry.getKey());
            system.append("\n\n").append(provider.queryGuide());
            // The write guide costs tokens and invites writes; ship it only when a binding of
            // this type actually allows one, so read-only canvases stay strictly read-only
            if (null != provider.writeGuide() && entry.getValue().stream().anyMatch(it -> !allowedWrites(it).isEmpty())) {
                system.append("\n\n").append(provider.writeGuide());
            }
            verify |= null != provider.verifyTool(entry.getValue());
        }
        if (verify) system.append("\n\n").append(VERIFY_PROMPT);
        system.append("\n\n# Data Models\n");
        if (bindings.size() > 1) {
            system.append("The page may read from every model listed below; join or combine them as the requirement demands, ")
                    .append("and use each model only through the Data Access section of its own data source type.\n");
        }
        byType.forEach((type, models) -> {
            CanvasModelProvider provider = this.provider(type);
            for (AiCanvasModel binding : models) {
                system.append("\n");
                if (byType.size() > 1) system.append("Data source type: `").append(type).append("`\n");
                system.append(provider.describe(binding.getModel()));
                if (StringUtils.isNotBlank(binding.getPurpose())) {
                    system.append("\nPurpose in this page: ").append(binding.getPurpose().trim());
                }
                // Only sources with a write guide can act on the switches; a read-only source
                // gets no line at all so the LLM is never tempted
                if (null != provider.writeGuide()) {
                    List<String> writes = allowedWrites(binding);
                    system.append("\nAllowed writes: ").append(writes.isEmpty() ? "none (read-only)" : String.join(", ", writes));
                }
            }
        });
        return system.toString();
    }

    // Write operations a binding allows, named after the SDK functions; empty means
    // read-only. Legacy rows created before the switches existed carry nulls = off
    public static List<String> allowedWrites(AiCanvasModel binding) {
        List<String> writes = new ArrayList<>();
        if (Boolean.TRUE.equals(binding.getAllowAdd())) writes.add("add");
        if (Boolean.TRUE.equals(binding.getAllowEdit())) writes.add("update");
        if (Boolean.TRUE.equals(binding.getAllowDelete())) writes.add("delete");
        return writes;
    }

    // Bindings in creation order (id), so the prompt is stable across rounds;
    // unsaved rows (null id) sort last
    public static List<AiCanvasModel> orderedBindings(AiCanvas view) {
        if (null == view.getModels()) return List.of();
        return view.getModels().stream()
                .sorted(Comparator.comparing(AiCanvasModel::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    // Group by data source type, keeping the first-seen order of types
    static Map<String, List<AiCanvasModel>> groupByType(List<AiCanvasModel> bindings) {
        return bindings.stream().collect(Collectors.groupingBy(AiCanvasModel::getDataType,
                LinkedHashMap::new, Collectors.toList()));
    }

    // ReAct contract shown to the LLM whenever the provider ships a verification tool
    private static final String VERIFY_PROMPT = """
            # Query Verification (ReAct)

            Tools are available in this conversation: metadata inspection tools plus verification tools. Every data query the page will run MUST be proven to work before it appears in the final document:

            1. Plan the data queries the page needs; use the inspection tools first whenever a structure, field code or option list you need is not already in the prompt — never guess it.
            2. Call the verification tool once per planned query, passing EXACTLY the same JSON argument the page will pass to the SDK function.
            3. If the call fails or the rows don't look as expected, fix the query (field codes, conditions, parameters) and verify again.
            4. Only verified queries may be embedded, and the page may only read row keys actually observed in the verified results.
            5. When a "Data Writes" section is present, every write is dry-run the same way through the write verification tool (nothing is persisted) before it is embedded.

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
