package xyz.erupt.monitor;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import xyz.erupt.core.annotation.EruptScan;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.module.ModuleInfo;
import xyz.erupt.tpl.service.EruptTplService;

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
public class EruptMonitorAutoConfiguration implements EruptModule {

    static {
        EruptModuleInvoke.addEruptModule(EruptMonitorAutoConfiguration.class);
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name("erupt-monitor").build();
    }

    @Override
    public List<MetaMenu> initMenus() {
        List<MetaMenu> metaMenus = new ArrayList<>();
        metaMenus.add(MetaMenu.createRootMenu("monitor", "系统监控", "fa fa-bullseye", 10));
        metaMenus.add(MetaMenu.createSimpleMenu("server.html", "服务监控", "server.html", metaMenus.get(0), 10, EruptTplService.TPL));
        metaMenus.add(MetaMenu.createSimpleMenu("redis.html", "缓存监控", "redis.html", metaMenus.get(0), 20, EruptTplService.TPL));
        return metaMenus;
    }
}
