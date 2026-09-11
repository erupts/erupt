package xyz.erupt.graph;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.module.ModuleInfo;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.graph.constant.GraphConstant;
import xyz.erupt.graph.service.EruptGraphService;
import xyz.erupt.tpl.service.EruptTplService;

import java.util.Collections;
import java.util.List;

/**
 * @author YuePeng
 */
@Configuration
@ComponentScan
public class EruptGraphAutoConfiguration implements EruptModule {

    static {
        EruptModuleInvoke.addEruptModule(EruptGraphAutoConfiguration.class);
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name("erupt-graph").description("Erupt model relationship graph").build();
    }

    @Override
    public void run() {
        // EruptModule callbacks run on a bare instance, not the Spring bean: resolve it here
        EruptSpringUtil.getBean(EruptGraphService.class).scanCubes();
    }

    @Override
    public List<MetaMenu> initMenus() {
        MetaMenu menu = MetaMenu.createSimpleMenu("erupt-graph", "Model Graph", GraphConstant.MENU_GRAPH,
                null, 60, EruptTplService.TPL);
        menu.setIcon("fa fa-diagram-project");
        return Collections.singletonList(menu);
    }

}
