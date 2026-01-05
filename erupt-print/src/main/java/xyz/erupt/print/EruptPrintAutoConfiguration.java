package xyz.erupt.print;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.ModuleInfo;
import xyz.erupt.upms.prop.EruptAppProp;

/**
 * @author YuePeng
 * date 2021/3/28 18:51
 */
@Configuration
@ComponentScan
@EnableConfigurationProperties
public class EruptPrintAutoConfiguration implements EruptModule {

    public static final String ERUPT_PRINT = "erupt-print";

    static {
        EruptModuleInvoke.addEruptModule(EruptPrintAutoConfiguration.class);
    }

    @Resource
    private EruptAppProp eruptAppProp;

    @PostConstruct
    public void post() {
        eruptAppProp.registerProp(ERUPT_PRINT, true);
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name(ERUPT_PRINT).description("Printing module").build();
    }

}
