package xyz.erupt.monitor;

import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import xyz.erupt.core.annotation.EruptScan;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.constant.MenuTypeEnum;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.module.ModuleInfo;
import xyz.erupt.monitor.constant.MonitorConstant;
import xyz.erupt.monitor.interceptor.HttpStatInterceptor;

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
public class EruptMonitorAutoConfiguration implements EruptModule, WebMvcConfigurer {

    @Resource
    private HttpStatInterceptor httpStatInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(httpStatInterceptor).addPathPatterns(EruptRestPath.ERUPT_API + "/**");
    }

    static {
        EruptModuleInvoke.addEruptModule(EruptMonitorAutoConfiguration.class);
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name("erupt-monitor").description("Erupt system monitor").build();
    }

    @Override
    public List<MetaMenu> initMenus() {
        List<MetaMenu> metaMenus = new ArrayList<>();
        String router = MenuTypeEnum.ROUTER.getCode();
        MetaMenu root = MetaMenu.createRootMenu("monitor", "System Monitoring", "fa fa-bullseye", 10);
        metaMenus.add(root);
        metaMenus.add(MetaMenu.createSimpleMenu(MonitorConstant.MENU_SERVER, "Service Monitoring", MonitorConstant.MENU_SERVER, root, 10, router));
        metaMenus.add(MetaMenu.createSimpleMenu(MonitorConstant.MENU_REDIS, "Cache Monitoring", MonitorConstant.MENU_REDIS, root, 20, router));
        metaMenus.add(MetaMenu.createSimpleMenu(MonitorConstant.MENU_DIAGNOSIS, "Diagnosis Monitoring", MonitorConstant.MENU_DIAGNOSIS, root, 30, router));
        return metaMenus;
    }
}
