package xyz.erupt.designer.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.view.EruptBuildModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.R;
import xyz.erupt.designer.model.DesignerEntity;
import xyz.erupt.designer.model.DesignerForm;
import xyz.erupt.designer.service.EruptCodeService;
import xyz.erupt.designer.service.EruptDesignerService;
import xyz.erupt.upms.annotation.EruptMenuAuth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author YuePeng
 * date 2026-06-12
 */
@RestController
@RequestMapping(EruptRestPath.ERUPT_API + "/designer")
@RequiredArgsConstructor
public class EruptDesignerController {

    // 设计器全部接口统一以 DesignerEntity 菜单作为权限标识
    public static final String MENU_AUTH = "DesignerEntity";

    private final EruptDesignerService eruptDesignerService;

    private final EruptCodeService eruptCodeService;

    // design json → disguised annotation EruptModel, rendered by the standard erupt frontend pipeline
    @PostMapping("/preview")
    @EruptMenuAuth(MENU_AUTH)
    public EruptBuildModel preview(@RequestBody DesignerForm designerForm) {
        return eruptDesignerService.preview(designerForm);
    }

    // load persisted design config of a model
    @GetMapping("/config/{className}")
    @EruptMenuAuth(MENU_AUTH)
    public R<Map<String, String>> config(@PathVariable("className") String className) {
        DesignerEntity entity = eruptDesignerService.loadDesign(className);
        Map<String, String> result = new HashMap<>();
        result.put("className", entity.getClassName());
        result.put("name", entity.getName());
        result.put("config", entity.getConfig());
        return R.ok(result);
    }

    // save design config and register the runtime erupt model, effective without restart
    @PostMapping("/publish/{className}")
    @EruptMenuAuth(MENU_AUTH)
    public R<Void> publish(@PathVariable("className") String className, @RequestBody DesignerForm designerForm) {
        eruptDesignerService.publish(className, designerForm);
        return R.ok();
    }

    // registered erupt model names, as options for reference field linking
    @GetMapping("/erupts")
    @EruptMenuAuth(MENU_AUTH)
    public R<List<String>> erupts() {
        return R.ok(EruptCoreService.getErupts().stream()
                .map(EruptModel::getEruptName).sorted().collect(Collectors.toList()));
    }

    // export annotation source code to graduate the design to hand-written development
    @PostMapping("/java-code")
    @EruptMenuAuth(MENU_AUTH)
    public R<String> javaCode(@RequestBody DesignerForm designerForm) {
        return R.ok(eruptCodeService.generate(designerForm));
    }

}
