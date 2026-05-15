package xyz.erupt.ai;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import xyz.erupt.ai.constants.AiConst;
import xyz.erupt.ai.model.*;
import xyz.erupt.core.annotation.EruptScan;
import xyz.erupt.core.constant.MenuTypeEnum;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.module.ModuleInfo;
import xyz.erupt.upms.prop.EruptAppProp;

import java.util.ArrayList;
import java.util.List;

/**
 * @author YuePeng
 * date 2025/2/22 00:20
 */
@Configuration
@ComponentScan
@EruptScan
@EntityScan
@EnableConfigurationProperties
@EnableAsync
@EnableScheduling
public class EruptAiAutoConfiguration implements EruptModule {

    @Resource
    private EruptAppProp eruptAppProp;

    static {
        EruptModuleInvoke.addEruptModule(EruptAiAutoConfiguration.class);
    }

    @PostConstruct
    public void post() {
        eruptAppProp.registerProp("erupt-ai", true);
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name("erupt-ai").description("The large-model-driven ERUPT AI infrastructure").build();
    }

    @Override
    public void run() {
        EruptModule.super.run();
    }

    @Override
    public List<MetaMenu> initMenus() {
        List<MetaMenu> menus = new ArrayList<>();
        menus.add(MetaMenu.createRootMenu("$ai", "AI Manager", "fa fa-magic", 25));
        menus.add(MetaMenu.createEruptClassMenu(LLM.class, menus.get(0), 10));
        menus.add(MetaMenu.createEruptClassMenu(McpServer.class, menus.get(0), 20));
        menus.add(MetaMenu.createEruptClassMenu(A2AAgent.class, menus.get(0), 30));
        menus.add(MetaMenu.createEruptClassMenu(LLMAgent.class, menus.get(0), 40));
        menus.add(MetaMenu.createEruptClassMenu(LLMRole.class, menus.get(0), 50));
        menus.add(MetaMenu.createSimpleMenu("ai-chat", "AI Chat", AiConst.AI_CHAT, menus.get(0), 100, MenuTypeEnum.ROUTER.getCode()));
        return menus;
    }

}
