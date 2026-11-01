package xyz.erupt.feishu;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.ModuleGroup;
import xyz.erupt.core.module.ModuleInfo;
import xyz.erupt.feishu.prop.EruptFeishuProperties;

/**
 * @author YuePeng
 */
@Configuration
@ComponentScan
@EnableConfigurationProperties(EruptFeishuProperties.class)
public class EruptFeishuAutoConfiguration implements EruptModule {

    static {
        EruptModuleInvoke.addEruptModule(EruptFeishuAutoConfiguration.class);
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name("erupt-data-feishu").group(ModuleGroup.DATA)
                .description("Feishu Bitable data source")
                .docs(ModuleInfo.eruptDocs("modules/erupt-feishu")).build();
    }

}
