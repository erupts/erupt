package xyz.erupt.upms;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import xyz.erupt.core.annotation.EruptScan;
import xyz.erupt.core.constant.MenuStatus;
import xyz.erupt.core.constant.MenuTypeEnum;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.module.ModuleInfo;
import xyz.erupt.upms.model.*;
import xyz.erupt.upms.model.log.EruptLoginLog;
import xyz.erupt.upms.model.log.EruptOperateLog;
import xyz.erupt.upms.model.online.EruptOnline;

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
@EnableConfigurationProperties
public class EruptUpmsAutoConfiguration implements EruptModule {

    static {
        EruptModuleInvoke.addEruptModule(EruptUpmsAutoConfiguration.class);
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name("erupt-upms").build();
    }

    @Override
    public List<MetaMenu> initMenus() {
        List<MetaMenu> menus = new ArrayList<>();
        menus.add(MetaMenu.createSimpleMenu("$home", "首页", "/", null, 0, "fa fa-home", MenuTypeEnum.ROUTER.getCode(), MenuStatus.OPEN));
        menus.add(MetaMenu.createRootMenu("$manager", "系统管理", "fa fa-cogs", 1));
        menus.add(MetaMenu.createEruptClassMenu(EruptMenu.class, menus.get(0), 0, MenuTypeEnum.TREE));
        menus.add(MetaMenu.createEruptClassMenu(EruptRole.class, menus.get(0), 10));
        menus.add(MetaMenu.createEruptClassMenu(EruptOrg.class, menus.get(0), 20, MenuTypeEnum.TREE));
        menus.add(MetaMenu.createEruptClassMenu(EruptPost.class, menus.get(0), 30, MenuTypeEnum.TREE));
        menus.add(MetaMenu.createEruptClassMenu(EruptUser.class, menus.get(0), 40));
        menus.add(MetaMenu.createEruptClassMenu(EruptDict.class, menus.get(0), 50));
        menus.add(MetaMenu.createEruptClassMenu(EruptDictItem.class, menus.get(0), 60, MenuStatus.HIDE));
        menus.add(MetaMenu.createEruptClassMenu(EruptOnline.class, menus.get(0), 65));
        menus.add(MetaMenu.createEruptClassMenu(EruptLoginLog.class, menus.get(0), 70));
        menus.add(MetaMenu.createEruptClassMenu(EruptOperateLog.class, menus.get(0), 80));
        return menus;
    }

}
