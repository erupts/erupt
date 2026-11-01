package xyz.erupt.es;

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
public class EruptEsAutoConfiguration implements EruptModule {

    static {
        EruptModuleInvoke.addEruptModule(EruptEsAutoConfiguration.class);
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name("erupt-data-es").group(ModuleGroup.DATA)
                .description("Elasticsearch data source")
                .docs(ModuleInfo.eruptDocs("modules/erupt-es")).build();
    }

}
