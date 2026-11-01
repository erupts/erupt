package xyz.erupt.decision;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import xyz.erupt.core.annotation.EruptScan;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.module.ModuleGroup;
import xyz.erupt.core.module.ModuleInfo;
import xyz.erupt.decision.model.DecisionDef;
import xyz.erupt.decision.model.DecisionModel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author YuePeng
 */
@Configuration
@ComponentScan
@EruptScan
@EntityScan
public class EruptAiDecisionAutoConfiguration implements EruptModule {

    static {
        EruptModuleInvoke.addEruptModule(EruptAiDecisionAutoConfiguration.class);
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name("erupt-ai-decision").group(ModuleGroup.AI)
                .description("Typed decisions the AI makes for a program to branch on")
                .docs(ModuleInfo.eruptDocs("modules/erupt-ai-decision")).build();
    }

    @Override
    public List<MetaMenu> initMenus() {
        List<MetaMenu> menus = new ArrayList<>();
        menus.add(MetaMenu.createRootMenu("$ai-decision", "AI Decision", "fa fa-code-branch", 140));
        menus.add(MetaMenu.createEruptClassMenu(DecisionDef.class, menus.get(0), 10));
        menus.add(MetaMenu.createEruptClassMenu(DecisionModel.class, menus.get(0), 20));
        return menus;
    }

}
