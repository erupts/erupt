package xyz.erupt.print;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import xyz.erupt.core.annotation.EruptScan;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.module.ModuleInfo;
import xyz.erupt.print.model.EruptPrintTpl;
import xyz.erupt.upms.prop.EruptAppProp;

import java.util.ArrayList;
import java.util.List;

/**
 * @author YuePeng
 * date 2021/3/28 18:51
 */
@EruptScan
@EntityScan
@Configuration
@ComponentScan
@EnableConfigurationProperties
@Component
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
        return ModuleInfo.builder().name(ERUPT_PRINT).description("Print module").build();
    }

    @Override
    public List<MetaMenu> initMenus() {
        List<MetaMenu> menus = new ArrayList<>();
        menus.add(MetaMenu.createRootMenu("$print", "Print Manager", "fa fa-print", 110));
        menus.add(MetaMenu.createEruptClassMenu(EruptPrintTpl.class, menus.get(0), 10));
        return menus;
    }
}
