package xyz.erupt.http;

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
public class EruptHttpAutoConfiguration implements EruptModule {

    static {
        EruptModuleInvoke.addEruptModule(EruptHttpAutoConfiguration.class);
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name("erupt-data-http").group(ModuleGroup.DATA)
                .description("REST API data source")
                .docs(ModuleInfo.eruptDocs("modules/erupt-http")).build();
    }

}
