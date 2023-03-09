package xyz.erupt.flow;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import xyz.erupt.core.annotation.EruptScan;
import xyz.erupt.core.constant.MenuTypeEnum;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.module.ModuleInfo;
import xyz.erupt.flow.constant.FlowConstant;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhu
 * date 2023/02/08
 */
@Configuration
@ComponentScan
@EntityScan
@EruptScan
@MapperScan("xyz.erupt.flow.mapper")
public class EruptFlowAutoConfiguration implements EruptModule {

    static {
        EruptModuleInvoke.addEruptModule(EruptFlowAutoConfiguration.class);
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name(FlowConstant.SERVER_NAME).build();
    }

    @Override
    public List<MetaMenu> initMenus() {
        List<MetaMenu> metaMenus = new ArrayList<>();
        metaMenus.add(MetaMenu.createRootMenu(FlowConstant.SERVER_NAME, "流程服务", "fa fa-bandcamp", 80));
        // 菜单的页面
        metaMenus.add(MetaMenu.createSimpleMenu("erupt-flow/workSpace", "工作区", "erupt-flow/workSpace"
                , metaMenus.get(0), 0, MenuTypeEnum.THIS_WINDOW.getCode()));
        metaMenus.add(MetaMenu.createSimpleMenu("erupt-flow/formsPanel", "流程设计器", "erupt-flow/formsPanel"
                , metaMenus.get(0), 10, MenuTypeEnum.THIS_WINDOW.getCode()));
        metaMenus.add(MetaMenu.createSimpleMenu("erupt-flow/formsPanel", "工单管理", "erupt-flow/formsPanel"
                , metaMenus.get(0), 20, MenuTypeEnum.THIS_WINDOW.getCode()));
        return metaMenus;
    }
}
