package xyz.erupt.atlas;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.module.ModuleInfo;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.atlas.constant.AtlasConstant;
import xyz.erupt.atlas.service.EruptAtlasService;
import xyz.erupt.tpl.service.EruptTplService;

import java.util.Collections;
import java.util.List;

/**
 * @author YuePeng
 */
@Configuration
@ComponentScan
public class EruptAtlasAutoConfiguration implements EruptModule {

    static {
        EruptModuleInvoke.addEruptModule(EruptAtlasAutoConfiguration.class);
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name("erupt-atlas").description("Erupt model atlas: relations, lineage and structural audit").build();
    }

    @Override
    public void run() {
        // EruptModule callbacks run on a bare instance, not the Spring bean: resolve it here
        EruptSpringUtil.getBean(EruptAtlasService.class).scanCubes();
    }

    @Override
    public List<MetaMenu> initMenus() {
        MetaMenu menu = MetaMenu.createSimpleMenu("erupt-atlas", "Model Atlas", AtlasConstant.MENU_ATLAS,
                null, 60, EruptTplService.TPL);
        menu.setIcon("fa fa-diagram-project");
        return Collections.singletonList(menu);
    }

}
