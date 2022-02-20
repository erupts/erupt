package xyz.erupt.generator;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import xyz.erupt.core.annotation.EruptScan;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.module.ModuleInfo;
import xyz.erupt.generator.model.GeneratorClass;

import java.util.ArrayList;
import java.util.List;

/**
 * @author YuePeng
 * date 2021/3/28 18:51
 */
@Configuration
@ComponentScan
@EntityScan
@EruptScan
@Component
public class EruptGeneratorAutoConfiguration implements EruptModule {

    static {
        EruptModuleInvoke.addEruptModule(EruptGeneratorAutoConfiguration.class);
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name("erupt-generator").build();
    }

    @Override
    public List<MetaMenu> initMenus() {
        List<MetaMenu> menus = new ArrayList<>();
        menus.add(MetaMenu.createRootMenu("$generator", "代码生成", "fa fa-code", 40));
        menus.add(MetaMenu.createEruptClassMenu(GeneratorClass.class, menus.get(0), 0));
        return menus;
    }
}
