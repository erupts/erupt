package xyz.erupt.file;

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
public class EruptFileAutoConfiguration implements EruptModule {

    static {
        EruptModuleInvoke.addEruptModule(EruptFileAutoConfiguration.class);
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name("erupt-data-file").group(ModuleGroup.DATA)
                .description("Files on disk as a data source: csv, json, yaml, properties, markdown and xml")
                .docs(ModuleInfo.eruptDocs("modules/erupt-file")).build();
    }

}
