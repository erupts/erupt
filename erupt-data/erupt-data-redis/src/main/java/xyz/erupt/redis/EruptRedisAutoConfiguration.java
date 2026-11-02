package xyz.erupt.redis;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.ModuleGroup;
import xyz.erupt.core.module.ModuleInfo;

/**
 * @author YuePeng
 */
@Configuration
@ComponentScan
public class EruptRedisAutoConfiguration implements EruptModule {

    static {
        EruptModuleInvoke.addEruptModule(EruptRedisAutoConfiguration.class);
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name("erupt-data-redis").group(ModuleGroup.DATA)
                .description("Redis data source")
                .docs(ModuleInfo.eruptDocs("modules/erupt-redis")).build();
    }

}
