package xyz.erupt.ai_claw;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import xyz.erupt.ai_claw.skill.EruptSkill;
import xyz.erupt.core.annotation.EruptScan;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.module.ModuleInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author YuePeng
 * date 2021/3/28 18:51
 */
@Configuration
@ComponentScan
@EntityScan
@EruptScan
@Component
public class EruptAiClawAutoConfiguration implements EruptModule {

    static {
        EruptModuleInvoke.addEruptModule(EruptAiClawAutoConfiguration.class);
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name("erupt-ai-claw").description("Drop-in admin agent built on erupt-ai — natural language to Erupt admin operations").build();
    }

    @Override
    public List<MetaMenu> initMenus() {
        List<MetaMenu> menus = new ArrayList<>();
        // Reuse the shared "AI Manager" root created by erupt-ai; menus are de-duplicated by code on persist
        MetaMenu ai = MetaMenu.createRootMenu("$ai", "AI Manager", "fa fa-magic", 25);
        menus.add(ai);
        menus.add(MetaMenu.createEruptClassMenu(EruptSkill.class, ai, 60));
        return menus;
    }

}
