package xyz.erupt.atlas;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import xyz.erupt.atlas.constant.AtlasConstant;
import xyz.erupt.atlas.model.EruptClassInfo;
import xyz.erupt.atlas.model.EruptFieldInfo;
import xyz.erupt.atlas.service.EruptAtlasService;
import xyz.erupt.core.annotation.EruptScan;
import xyz.erupt.core.constant.MenuStatus;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.module.ModuleInfo;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.tpl.service.EruptTplService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author YuePeng
 */
@Configuration
@ComponentScan
@EruptScan
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
        List<MetaMenu> menus = new ArrayList<>();
        MetaMenu root = MetaMenu.createRootMenu(AtlasConstant.MENU_ROOT, "Model Atlas", "fa fa-diagram-project", 60);
        menus.add(root);
        // Graph and registry read the same live registry: the graph for relations, the table for search and publish
        menus.add(MetaMenu.createSimpleMenu("erupt-atlas", "Model Graph", AtlasConstant.MENU_ATLAS, root, 10, EruptTplService.TPL));
        menus.add(MetaMenu.createEruptClassMenu(EruptClassInfo.class, root, 20));
        // Drill target of the class registry only; never shown in the sidebar
        menus.add(MetaMenu.createEruptClassMenu(EruptFieldInfo.class, root, 30, MenuStatus.HIDE));
        return menus;
    }

}
