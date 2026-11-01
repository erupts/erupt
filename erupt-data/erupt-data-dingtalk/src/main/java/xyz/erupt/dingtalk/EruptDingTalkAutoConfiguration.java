package xyz.erupt.dingtalk;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.ModuleGroup;
import xyz.erupt.core.module.ModuleInfo;
import xyz.erupt.dingtalk.prop.EruptDingTalkProperties;

/**
 * @author YuePeng
 */
@Configuration
@ComponentScan
@EnableConfigurationProperties(EruptDingTalkProperties.class)
public class EruptDingTalkAutoConfiguration implements EruptModule {

    static {
        EruptModuleInvoke.addEruptModule(EruptDingTalkAutoConfiguration.class);
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name("erupt-data-dingtalk").group(ModuleGroup.DATA)
                .description("DingTalk Notable data source")
                .docs(ModuleInfo.eruptDocs("modules/erupt-dingtalk")).build();
    }

}
