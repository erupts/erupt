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
// 仅扫描 model 包，模板类（EruptDesignerTemplate）只作为注解实例来源，不注册为业务模型
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
        // 重注册已发布的设计模型（伪装注解，运行时生效）
        eruptDesignerService.registerAll();
    }

    @Override
    public List<MetaMenu> initMenus() {
        List<MetaMenu> menus = new ArrayList<>();
        menus.add(MetaMenu.createRootMenu("$designer", "Form Designer", "fa fa-object-group", 45));
        menus.add(MetaMenu.createEruptClassMenu(DesignerEntity.class, menus.get(0), 0));
        return menus;
    }

}
