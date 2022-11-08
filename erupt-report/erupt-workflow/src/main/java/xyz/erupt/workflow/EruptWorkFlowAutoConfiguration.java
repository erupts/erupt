package xyz.erupt.workflow;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import xyz.erupt.core.annotation.EruptScan;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.ModuleInfo;

/**
 * @author YuePeng
 * date 2022/11/8 21:25
 */
@Configuration
@ComponentScan
@EntityScan
@EruptScan
@Component
@EnableConfigurationProperties
public class EruptWorkFlowAutoConfiguration implements EruptModule {

    static {
        EruptModuleInvoke.addEruptModule(EruptWorkFlowAutoConfiguration.class);
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name("erupt-workflow").build();
    }



}
