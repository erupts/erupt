package xyz.erupt.ai;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import xyz.erupt.ai.model.LLM;
import xyz.erupt.ai.model.LLMAgent;
import xyz.erupt.core.annotation.EruptScan;
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
        return ModuleInfo.builder().name("erupt-ai").build();
    }

    @Override
    public void run() {
        EruptModule.super.run();
    }

    @Override
    public List<MetaMenu> initMenus() {
        List<MetaMenu> menus = new ArrayList<>();
        menus.add(MetaMenu.createRootMenu("$ai", "大模型管理", "fa fa-rocket", 25));
        menus.add(MetaMenu.createEruptClassMenu(LLM.class, menus.get(0), 10));
        menus.add(MetaMenu.createEruptClassMenu(LLMAgent.class, menus.get(0), 20));
        return menus;
    }

}
