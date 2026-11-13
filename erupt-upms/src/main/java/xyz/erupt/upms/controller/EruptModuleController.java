package xyz.erupt.upms.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.view.R;
import xyz.erupt.upms.service.EruptMenuService;
import xyz.erupt.upms.service.EruptModuleCenterService;
import xyz.erupt.upms.vo.ModuleVo;

import java.util.List;

/**
 * Module center
 *
 * @order 20
 * @apiNote The module center page: installed modules by group, switched on and off. Every call is checked against the page's menu.
 *
 * @author YuePeng
 * date 2026-11-01
 */
@RestController
@RequestMapping(EruptRestPath.ERUPT_API + "/modules")
public class EruptModuleController implements EruptRouter.VerifyHandler {

    /** The erupt-web route, and the value of the page's menu. */
    public static final String PAGE = "/module-center";

    @Resource
    private EruptModuleCenterService moduleCenterService;

    @Resource
    private EruptMenuService eruptMenuService;

    @Override
    public String convertAuthStr(EruptRouter eruptRouter, HttpServletRequest request, String authStr) {
        return PAGE;
    }

    /**
     * Every registered module.
     *
     * @return modules in the groups' order, the group given as a translated title
     */
    @GetMapping
    @EruptRouter(verifyType = EruptRouter.VerifyType.MENU, verifyHandler = EruptModuleController.class)
    public List<ModuleVo> modules() {
        return moduleCenterService.modules();
    }

    /**
     * Switch a module's menus on or off.
     *
     * @param name  module name
     * @param value true to switch on
     * @return ok
     */
    @PostMapping("/{name}/enabled")
    @EruptRouter(verifyType = EruptRouter.VerifyType.MENU, verifyHandler = EruptModuleController.class)
    public R<Void> enabled(@PathVariable("name") String name, @RequestParam("value") boolean value) {
        moduleCenterService.enabled(name, value);
        eruptMenuService.flushMenuCache();
        return R.ok();
    }

}
