package xyz.erupt.ai_dev;

import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import xyz.erupt.ai_dev.model.DevProject;
import xyz.erupt.ai_dev.model.DevRequirement;
import xyz.erupt.ai_dev.service.DevExecutor;
import xyz.erupt.core.annotation.EruptScan;
import xyz.erupt.ai_dev.controller.DevPoolController;
import xyz.erupt.core.constant.MenuStatus;
import xyz.erupt.core.constant.MenuTypeEnum;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.module.ModuleInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author YuePeng
 * date 2026/10/21
 */
@Configuration
@ComponentScan
@EntityScan
@EruptScan
@Component
public class EruptAiDevAutoConfiguration implements EruptModule {

    static {
        EruptModuleInvoke.addEruptModule(EruptAiDevAutoConfiguration.class);
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name("erupt-ai-dev").description("AI software delivery — projects, requirement pool and worktrees for coding agents").build();
    }

    @Resource
    private DevExecutor devExecutor;

    @Override
    public void run() {
        devExecutor.start();
    }

    @Override
    public List<MetaMenu> initMenus() {
        List<MetaMenu> menus = new ArrayList<>();
        menus.add(MetaMenu.createRootMenu("$ai-dev", "AI Dev", "fa fa-laptop-code", 28));
        menus.add(MetaMenu.createSimpleMenu("ai-dev-pool", "Requirement Pool", DevPoolController.PAGE, menus.get(0), 5, MenuTypeEnum.ROUTER.getCode()));
        menus.add(MetaMenu.createEruptClassMenu(DevProject.class, menus.get(0), 10));
        // the pool page is the way in; the model stays for its own API and permissions, which a hidden menu still grants
        menus.add(MetaMenu.createEruptClassMenu(DevRequirement.class, menus.get(0), 20, MenuStatus.HIDE));
        return menus;
    }

}
