package xyz.erupt.flow;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
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
@EnableJpaRepositories
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
        //目录名和下面的权限名重复，所以加个后缀
        metaMenus.add(MetaMenu.createRootMenu(FlowConstant.SERVER_NAME + "_root", "流程服务", "fa fa-bandcamp", 80));
        // 添加菜单
        metaMenus.add(MetaMenu.createSimpleMenu(FlowConstant.SERVER_NAME, "流程服务基础权限", "erupt-flow"
                , metaMenus.get(0), 0, MenuTypeEnum.BUTTON.getCode()));
        metaMenus.add(MetaMenu.createSimpleMenu("workSpace", "工作区", "erupt-flow/index.html#/workSpace"
                , metaMenus.get(0), 10, MenuTypeEnum.LINK.getCode()));
        metaMenus.add(MetaMenu.createSimpleMenu("formsPanel", "后台管理", "erupt-flow/index.html#/formsPanel"
                , metaMenus.get(0), 20, MenuTypeEnum.LINK.getCode()));
        metaMenus.add(MetaMenu.createSimpleMenu("OaProcessInstanceHistory", "流程实例", "OaProcessInstanceHistory"
                , metaMenus.get(0), 30, MenuTypeEnum.TABLE.getCode()));
        metaMenus.add(MetaMenu.createSimpleMenu("OaTaskHistory", "任务", "OaTaskHistory"
                , metaMenus.get(0), 40, MenuTypeEnum.TABLE.getCode()));
        metaMenus.add(MetaMenu.createSimpleMenu("OaTaskOperation", "操作记录", "OaTaskOperation"
                , metaMenus.get(0), 50, MenuTypeEnum.TABLE.getCode()));
        return metaMenus;
    }
}
