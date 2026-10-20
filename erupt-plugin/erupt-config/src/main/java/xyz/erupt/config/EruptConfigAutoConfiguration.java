package xyz.erupt.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import xyz.erupt.config.model.ConfigEffectiveValue;
import xyz.erupt.config.model.ConfigItem;
import xyz.erupt.config.model.ConfigProfile;
import xyz.erupt.core.constant.MenuStatus;
import xyz.erupt.core.annotation.EruptScan;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.module.ModuleInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Runtime settings inside the admin: key/value items per Spring profile, layered like the
 * {@code application-{profile}.yml} files. The items of the active profiles are a property source,
 * so {@code @Value("${key}")} and {@code Environment#getProperty} read them, and a saved change
 * reaches the running beans without a restart.
 *
 * @author YuePeng
 */
@Configuration
@ComponentScan
@EruptScan
@EntityScan
public class EruptConfigAutoConfiguration implements EruptModule {

    static {
        EruptModuleInvoke.addEruptModule(EruptConfigAutoConfiguration.class);
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name("erupt-config").description("Runtime settings per Spring profile").build();
    }

    @Override
    public List<MetaMenu> initMenus() {
        List<MetaMenu> menus = new ArrayList<>();
        MetaMenu root = MetaMenu.createRootMenu("$config", "Config", "fa fa-sliders", 54);
        menus.add(root);
        menus.add(MetaMenu.createEruptClassMenu(ConfigItem.class, root, 10));
        menus.add(MetaMenu.createEruptClassMenu(ConfigEffectiveValue.class, root, 15));
        // managed in the tree beside the items; hidden so the model still carries its permissions
        menus.add(MetaMenu.createEruptClassMenu(ConfigProfile.class, root, 20, MenuStatus.HIDE));
        return menus;
    }

}
