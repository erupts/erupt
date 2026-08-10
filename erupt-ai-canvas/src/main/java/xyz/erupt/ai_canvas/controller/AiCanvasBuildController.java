package xyz.erupt.ai_canvas.controller;

import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import xyz.erupt.ai.config.AiProp;
import xyz.erupt.ai.model.LLM;
import xyz.erupt.ai_canvas.model.AiCanvas;
import xyz.erupt.ai_canvas.model.AiCanvasVersion;
import xyz.erupt.ai_canvas.service.AiCanvasService;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.view.R;
import xyz.erupt.jpa.dao.EruptDao;

import java.util.ArrayList;
import java.util.List;

/**
 * REST surface of the view designer: data source discovery, conversational
 * generation and version switching.
 *
 * @author YuePeng
 * date 2026/8/4
 */
@RestController
@RequestMapping(EruptRestPath.ERUPT_API + "/ai-canvas/build")
public class AiCanvasBuildController {

    @Resource
    private AiCanvasService aiViewService;

    @Resource
    private EruptDao eruptDao;

    @Resource
    private AiProp aiProp;

    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    @GetMapping("/models")
    public R<List<ModelGroup>> models() {
        List<ModelGroup> groups = new ArrayList<>();
        aiViewService.getProviders().forEach((type, provider) -> {
            ModelGroup group = new ModelGroup();
            group.setType(type);
            group.setModels(provider.models());
            groups.add(group);
        });
        return R.ok(groups);
    }

    // Light projection: demoHtml stays server-side, it is only prompt material
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    @GetMapping("/styles")
    public R<List<StyleVo>> styles() {
        return R.ok(aiViewService.getStyles().stream().map(StyleVo::new).toList());
    }

    // Enabled chat models the designer can pick from; the default one leads
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    @GetMapping("/llms")
    public R<List<LlmVo>> llms() {
        return R.ok(eruptDao.lambdaQuery(LLM.class).eq(LLM::getEnable, true)
                .orderByDesc(LLM::getDefaultLLM).list().stream().map(LlmVo::new).toList());
    }

    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    @GetMapping("/{code}")
    public R<DesignerVo> info(@PathVariable("code") String code) {
        AiCanvas view = this.view(code);
        DesignerVo vo = new DesignerVo();
        vo.setName(view.getName());
        vo.setDataType(view.getDataType());
        vo.setTargetModel(view.getTargetModel());
        vo.setStyle(view.getStyle());
        vo.setLlmId(null != view.getLlm() ? view.getLlm().getId() : null);
        vo.setActiveVersion(view.getActiveVersion());
        vo.setVersions(eruptDao.lambdaQuery(AiCanvasVersion.class)
                .eq(AiCanvasVersion::getCanvasId, view.getId())
                .orderByAsc(AiCanvasVersion::getVersion).list().stream().map(VersionVo::new).toList());
        return R.ok(vo);
    }

    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    @PostMapping("/generate/{code}")
    public R<VersionVo> generate(@PathVariable("code") String code, @RequestBody GenerateBody body) {
        if (StringUtils.isBlank(body.getMessage())) {
            throw new EruptWebApiRuntimeException("Message must not be blank");
        }
        AiCanvas view = this.view(code);
        view.setDataType(body.getDataType());
        view.setTargetModel(body.getTargetModel());
        view.setStyle(body.getStyle());
        view.setLlm(this.resolveLlm(body.getLlmId()));
        return R.ok(new VersionVo(aiViewService.generate(view, body.getMessage().trim())));
    }

    // Streaming variant of generate; EventSource is GET-only, so the token
    // arrives as the _token URL parameter (PARAM verify)
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN, verifyMethod = EruptRouter.VerifyMethod.PARAM)
    @GetMapping(value = "/generate-sse/{code}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter generateSse(@PathVariable("code") String code,
                                  @RequestParam("message") String message,
                                  @RequestParam("dataType") String dataType,
                                  @RequestParam("targetModel") String targetModel,
                                  @RequestParam(value = "style", required = false) String style,
                                  @RequestParam(value = "llmId", required = false) Long llmId) {
        if (StringUtils.isBlank(message)) {
            throw new EruptWebApiRuntimeException("Message must not be blank");
        }
        AiCanvas view = this.view(code);
        view.setDataType(dataType);
        view.setTargetModel(targetModel);
        view.setStyle(style);
        view.setLlm(this.resolveLlm(llmId));
        // Persist the selection right away; html and version follow when the stream finishes
        eruptDao.mergeAndFlush(view);
        eruptDao.detach(view);
        SseEmitter emitter = new SseEmitter(aiProp.getSseTimeout());
        aiViewService.generateSse(MetaContext.get(), view, message.trim(), emitter);
        return emitter;
    }

    // Explicit stop: the running round is discarded; without this signal a mere
    // disconnect (page refresh) still persists the generated version
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    @PostMapping("/stop/{code}")
    public R<Void> stop(@PathVariable("code") String code) {
        aiViewService.stopGenerate(this.view(code).getId());
        return R.ok();
    }

    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    @PostMapping("/active/{code}/{versionId}")
    public R<Void> active(@PathVariable("code") String code, @PathVariable("versionId") Long versionId) {
        AiCanvas view = this.view(code);
        AiCanvasVersion version = eruptDao.find(AiCanvasVersion.class, versionId);
        if (null == version || !view.getId().equals(version.getCanvasId())) {
            throw new EruptWebApiRuntimeException("Version not found: " + versionId);
        }
        aiViewService.activate(view, version);
        return R.ok();
    }

    private AiCanvas view(String code) {
        AiCanvas view = eruptDao.lambdaQuery(AiCanvas.class).eq(AiCanvas::getCode, code).one();
        if (null == view) throw new EruptWebApiRuntimeException("View not found: " + code);
        return view;
    }

    // null id means "use the default chat model", resolved at generation time
    private LLM resolveLlm(Long llmId) {
        return null == llmId ? null : eruptDao.find(LLM.class, llmId);
    }

    @Getter
    @Setter
    public static class ModelGroup {
        private String type;
        private List<VLModel> models;
    }

    @Getter
    @Setter
    public static class GenerateBody {
        private String message;
        private String dataType;
        private String targetModel;
        private String style;
        private Long llmId;
    }

    @Getter
    @Setter
    public static class LlmVo {
        private Long id;
        private String name;
        private Boolean defaultLLM;

        public LlmVo(LLM llm) {
            this.id = llm.getId();
            this.name = llm.getName();
            this.defaultLLM = llm.getDefaultLLM();
        }
    }

    @Getter
    @Setter
    public static class DesignerVo {
        private String name;
        private String dataType;
        private String targetModel;
        private String style;
        private Long llmId;
        private Long activeVersion;
        private List<VersionVo> versions;
    }

    @Getter
    @Setter
    public static class StyleVo {
        private String id;
        private String name;
        private String description;
        private String mode;
        private List<String> palette;

        public StyleVo(AiCanvasService.CanvasStyle style) {
            this.id = style.getId();
            this.name = style.getName();
            this.description = style.getDescription();
            if (null != style.getSystem()) {
                this.mode = style.getSystem().getMode();
                this.palette = style.getSystem().getChartPalette();
            }
        }
    }

    @Getter
    @Setter
    public static class VersionVo {
        private Long id;
        private Integer version;
        private String message;
        private String dataType;
        private String targetModel;
        private String style;
        private String createTime;

        public VersionVo(AiCanvasVersion v) {
            this.id = v.getId();
            this.version = v.getVersion();
            this.message = v.getMessage();
            this.dataType = v.getDataType();
            this.targetModel = v.getTargetModel();
            this.style = v.getStyle();
            this.createTime = null == v.getCreateTime() ? null : v.getCreateTime().toString();
        }
    }

}
