package xyz.erupt.es;

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
public class EruptEsAutoConfiguration implements EruptModule {

    static {
        EruptModuleInvoke.addEruptModule(EruptEsAutoConfiguration.class);
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name("erupt-es").description("Elasticsearch data source implement").build();
    }

}
