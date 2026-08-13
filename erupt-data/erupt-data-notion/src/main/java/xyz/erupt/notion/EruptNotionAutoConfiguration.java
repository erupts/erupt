package xyz.erupt.notion;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.ModuleInfo;
import xyz.erupt.notion.prop.EruptNotionProperties;

/**
 * @author YuePeng
 */
@Configuration
@ComponentScan
@EnableConfigurationProperties(EruptNotionProperties.class)
public class EruptNotionAutoConfiguration implements EruptModule {

    static {
        EruptModuleInvoke.addEruptModule(EruptNotionAutoConfiguration.class);
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name("erupt-data-notion").description("Notion database data source implement").build();
    }

}
