package xyz.erupt.tpl;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.core.constant.MenuTypeEnum;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.ModuleInfo;
import xyz.erupt.tpl.service.EruptTplService;

import javax.annotation.Resource;

/**
 * @author YuePeng
 * date 2021/3/28 18:51
 */
@Configuration
@ComponentScan
@Component
@EnableConfigurationProperties
public class EruptTplAutoConfiguration implements EruptModule {

    static {
        EruptModuleInvoke.addEruptModule(EruptTplAutoConfiguration.class);
        String tip = "File Name under the tpl Directory";
        MenuTypeEnum.addMenuType(new VLModel(EruptTplService.TPL, "menu_type_tpl_iframe", tip));
        MenuTypeEnum.addMenuType(new VLModel(EruptTplService.TPL_MICRO, "menu_type_tpl_micro", tip));
    }

    @Resource
    private EruptTplService eruptTplService;

    @Override
    public void run() {
        eruptTplService.run();
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name("erupt-tpl").build();
    }

}
