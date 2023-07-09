package xyz.erupt.cloud.server;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import xyz.erupt.cloud.server.base.CloudServerConst;
import xyz.erupt.cloud.server.model.CloudNode;
import xyz.erupt.cloud.server.model.CloudNodeGroup;
import xyz.erupt.core.annotation.EruptScan;
import xyz.erupt.core.constant.MenuTypeEnum;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.module.ModuleInfo;

import java.util.Arrays;
import java.util.List;

/**
 * @author YuePeng
 * date 2021/12/16 00:15
 */
@Configuration
@ComponentScan
@EruptScan
@EntityScan
@EnableConfigurationProperties
public class EruptCloudServerAutoConfiguration implements EruptModule {

    static {
        EruptModuleInvoke.addEruptModule(EruptCloudServerAutoConfiguration.class);
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name("erupt-cloud-server").build();
    }

    @Override
    public List<MetaMenu> initMenus() {
        MetaMenu nodeManager = MetaMenu.createRootMenu("$NodeManager", "微节点管理", "fa fa-cloud", 70);
        MetaMenu nodeMenu = MetaMenu.createEruptClassMenu(CloudNode.class, nodeManager, 20);
        return Arrays.asList(nodeManager,
                MetaMenu.createEruptClassMenu(CloudNodeGroup.class, nodeManager, 10, MenuTypeEnum.TREE), nodeMenu,
                MetaMenu.createSimpleMenu(
                        CloudServerConst.CLOUD_ACCESS_TOKEN_PERMISSION, "查看令牌",
                        CloudServerConst.CLOUD_ACCESS_TOKEN_PERMISSION, nodeMenu, 20, MenuTypeEnum.BUTTON.getCode()
                ),
                MetaMenu.createSimpleMenu(
                        CloudServerConst.CLOUD_NODE_MANAGER_PERMISSION, "节点管理",
                        CloudServerConst.CLOUD_NODE_MANAGER_PERMISSION, nodeMenu, 30, MenuTypeEnum.BUTTON.getCode()
                )
        );
    }

}
