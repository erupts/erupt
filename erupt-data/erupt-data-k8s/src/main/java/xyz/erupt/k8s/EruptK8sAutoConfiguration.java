package xyz.erupt.k8s;

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
public class EruptK8sAutoConfiguration implements EruptModule {

    static {
        EruptModuleInvoke.addEruptModule(EruptK8sAutoConfiguration.class);
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name("erupt-data-k8s").description("Kubernetes resources data source implement").build();
    }

}
