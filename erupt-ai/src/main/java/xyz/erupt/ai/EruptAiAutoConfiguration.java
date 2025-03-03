package xyz.erupt.ai;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import xyz.erupt.core.annotation.EruptScan;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.module.ModuleInfo;

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

    static {
        EruptModuleInvoke.addEruptModule(EruptAiAutoConfiguration.class);
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
        return EruptModule.super.initMenus();
    }

}
