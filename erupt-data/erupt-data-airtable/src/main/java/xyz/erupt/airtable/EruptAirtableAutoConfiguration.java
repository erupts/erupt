package xyz.erupt.airtable;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import xyz.erupt.airtable.prop.EruptAirtableProperties;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.ModuleInfo;

/**
 * @author YuePeng
 */
@Configuration
@ComponentScan
@EnableConfigurationProperties(EruptAirtableProperties.class)
public class EruptAirtableAutoConfiguration implements EruptModule {

    static {
        EruptModuleInvoke.addEruptModule(EruptAirtableAutoConfiguration.class);
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name("erupt-data-airtable").description("Airtable data source implement").build();
    }

}
