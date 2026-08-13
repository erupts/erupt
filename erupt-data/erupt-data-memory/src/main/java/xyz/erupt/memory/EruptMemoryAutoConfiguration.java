package xyz.erupt.memory;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.ModuleInfo;

/**
 * @author YuePeng
 */
@Configuration
@ComponentScan
public class EruptMemoryAutoConfiguration implements EruptModule {

    static {
        EruptModuleInvoke.addEruptModule(EruptMemoryAutoConfiguration.class);
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name("erupt-data-memory").description("In-memory data source implement").build();
    }

}
