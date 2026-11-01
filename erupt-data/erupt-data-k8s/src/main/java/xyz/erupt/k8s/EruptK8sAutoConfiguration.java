package xyz.erupt.k8s;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import xyz.erupt.core.annotation.EruptScan;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.ModuleGroup;
import xyz.erupt.core.module.ModuleInfo;

/**
 * @author YuePeng
 */
@Configuration
@ComponentScan
// Registers the row-operation forms (K8sScaleForm)
@EruptScan
public class EruptK8sAutoConfiguration implements EruptModule {

    static {
        EruptModuleInvoke.addEruptModule(EruptK8sAutoConfiguration.class);
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name("erupt-data-k8s").group(ModuleGroup.DATA)
                .description("Kubernetes resources as a data source")
                .docs(ModuleInfo.eruptDocs("modules/erupt-k8s")).build();
    }

}
