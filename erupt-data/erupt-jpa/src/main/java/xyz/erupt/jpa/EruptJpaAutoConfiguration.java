package xyz.erupt.jpa;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.ModuleInfo;

/**
 * @author YuePeng
 * date 2021/3/28 18:51
 */
@Configuration
@ComponentScan
public class EruptJpaAutoConfiguration implements EruptModule {

    static {
        EruptModuleInvoke.addEruptModule(EruptJpaAutoConfiguration.class);
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name("erupt-jpa").build();
    }

}
