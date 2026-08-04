package xyz.erupt.ai_canvas.controller;

import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import xyz.erupt.ai.config.AiProp;
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

    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    @GetMapping("/{canvasId}")
    public R<DesignerVo> info(@PathVariable("canvasId") Long canvasId) {
        AiCanvas view = this.view(canvasId);
        DesignerVo vo = new DesignerVo();
        vo.setName(view.getName());
        vo.setDataType(view.getDataType());
        vo.setTargetModel(view.getTargetModel());
        vo.setStyle(view.getStyle());
        vo.setActiveVersion(view.getActiveVersion());
        vo.setVersions(eruptDao.lambdaQuery(AiCanvasVersion.class)
                .eq(AiCanvasVersion::getCanvasId, canvasId)
                .orderByAsc(AiCanvasVersion::getVersion).list().stream().map(VersionVo::new).toList());
        return R.ok(vo);
    }

    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    @PostMapping("/generate/{canvasId}")
    public R<VersionVo> generate(@PathVariable("canvasId") Long canvasId, @RequestBody GenerateBody body) {
        if (StringUtils.isBlank(body.getMessage())) {
            throw new EruptWebApiRuntimeException("Message must not be blank");
        }
        AiCanvas view = this.view(canvasId);
        view.setDataType(body.getDataType());
        view.setTargetModel(body.getTargetModel());
        view.setStyle(body.getStyle());
        return R.ok(new VersionVo(aiViewService.generate(view, body.getMessage().trim())));
    }

    // Streaming variant of generate; EventSource is GET-only, so the token
    // arrives as the _token URL parameter (PARAM verify, same as the render endpoint)
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN, verifyMethod = EruptRouter.VerifyMethod.PARAM)
    @GetMapping(value = "/generate-sse/{canvasId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter generateSse(@PathVariable("canvasId") Long canvasId,
                                  @RequestParam("message") String message,
                                  @RequestParam("dataType") String dataType,
                                  @RequestParam("targetModel") String targetModel,
                                  @RequestParam(value = "style", required = false) String style) {
        if (StringUtils.isBlank(message)) {
            throw new EruptWebApiRuntimeException("Message must not be blank");
        }
        AiCanvas view = this.view(canvasId);
        view.setDataType(dataType);
        view.setTargetModel(targetModel);
        view.setStyle(style);
        // Persist the selection right away; html and version follow when the stream finishes
        eruptDao.mergeAndFlush(view);
        eruptDao.detach(view);
        SseEmitter emitter = new SseEmitter(aiProp.getSseTimeout());
        aiViewService.generateSse(MetaContext.get(), view, message.trim(), emitter);
        return emitter;
    }

    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    @PostMapping("/active/{canvasId}/{versionId}")
    public R<Void> active(@PathVariable("canvasId") Long canvasId, @PathVariable("versionId") Long versionId) {
        AiCanvas view = this.view(canvasId);
        AiCanvasVersion version = eruptDao.find(AiCanvasVersion.class, versionId);
        if (null == version || !view.getId().equals(version.getCanvasId())) {
            throw new EruptWebApiRuntimeException("Version not found: " + versionId);
        }
        aiViewService.activate(view, version);
        return R.ok();
    }

    private AiCanvas view(Long canvasId) {
        AiCanvas view = eruptDao.find(AiCanvas.class, canvasId);
        if (null == view) throw new EruptWebApiRuntimeException("View not found: " + canvasId);
        return view;
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
    }

    @Getter
    @Setter
    public static class DesignerVo {
        private String name;
        private String dataType;
        private String targetModel;
        private String style;
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
