package xyz.erupt.job;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import xyz.erupt.core.annotation.EruptScan;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.module.ModuleInfo;
import xyz.erupt.job.model.EruptJob;
import xyz.erupt.job.model.EruptJobLog;
import xyz.erupt.job.model.EruptMail;

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
public class EruptJobAutoConfiguration implements EruptModule {

    static {
        EruptModuleInvoke.addEruptModule(EruptJobAutoConfiguration.class);
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name("erupt-job").build();
    }

    @Override
    public List<MetaMenu> initMenus() {
        List<MetaMenu> menus = new ArrayList<>();
        menus.add(MetaMenu.createRootMenu("$job", "任务管理", "fa fa-cubes", 30));
        menus.add(MetaMenu.createEruptClassMenu(EruptJob.class, menus.get(0), 0));
        menus.add(MetaMenu.createEruptClassMenu(EruptJobLog.class, menus.get(0), 10));
        menus.add(MetaMenu.createEruptClassMenu(EruptMail.class, menus.get(0), 20));
        return menus;
    }
}
