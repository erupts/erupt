package xyz.erupt.designer;

import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import xyz.erupt.core.annotation.EruptScan;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.module.ModuleInfo;
import xyz.erupt.designer.model.DesignerEntity;
import xyz.erupt.designer.service.EruptDesignerService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author YuePeng
 * date 2026-06-12
 */
@Configuration
@ComponentScan
@EntityScan
// scan only the model package; EruptDesignerTemplate is an annotation-instance source only, not a business model
@EruptScan("xyz.erupt.designer.model")
@Component
public class EruptDesignerAutoConfiguration implements EruptModule {

    static {
        EruptModuleInvoke.addEruptModule(EruptDesignerAutoConfiguration.class);
    }

    @Resource
    private EruptDesignerService eruptDesignerService;

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name("erupt-designer").description("Visual erupt form designer").build();
    }

    @Override
    public void run() {
        // re-register all published design models (disguised annotations, effective at runtime)
        eruptDesignerService.registerAll();
    }

    @Override
    public List<MetaMenu> initMenus() {
        List<MetaMenu> menus = new ArrayList<>();
        MetaMenu menu = MetaMenu.createEruptClassMenu(DesignerEntity.class, null, 45);
        menu.setIcon("fa fa-object-group");
        menus.add(menu);
        return menus;
    }

}
