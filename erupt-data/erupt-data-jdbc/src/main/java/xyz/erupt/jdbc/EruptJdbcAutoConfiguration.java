package xyz.erupt.jdbc;

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
public class EruptJdbcAutoConfiguration implements EruptModule {

    static {
        EruptModuleInvoke.addEruptModule(EruptJdbcAutoConfiguration.class);
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name("erupt-data-jdbc").description("Plain jdbc data source implement").build();
    }

}
