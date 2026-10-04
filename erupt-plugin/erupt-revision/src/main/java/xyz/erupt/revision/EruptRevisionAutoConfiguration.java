package xyz.erupt.revision;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import xyz.erupt.core.annotation.EruptScan;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.module.ModuleInfo;
import xyz.erupt.upms.prop.EruptAppProp;
import xyz.erupt.revision.model.EruptRecordRevision;
import xyz.erupt.upms.EruptUpmsAutoConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * Field-level change history for every erupt record: each add, update and delete that goes
 * through the erupt data pipeline leaves a revision row holding what changed, and an update
 * can be rolled back from the record panel. Opt-in by adding this module; a model opts out
 * with {@code @Power(revision = false)}. The frontend reads the {@link #ERUPT_REVISION}
 * property to show the entry.
 *
 * @author YuePeng
 * date 2026/10/3
 */
@Configuration
@ComponentScan
@EruptScan
@EntityScan
public class EruptRevisionAutoConfiguration implements EruptModule {

    static {
        EruptModuleInvoke.addEruptModule(EruptRevisionAutoConfiguration.class);
    }

    public static final String ERUPT_REVISION = "erupt-revision";

    @Resource
    private EruptAppProp eruptAppProp;

    @PostConstruct
    public void post() {
        eruptAppProp.registerProp(ERUPT_REVISION, true);
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name(ERUPT_REVISION).description("Record revisions").build();
    }

    @Override
    public List<MetaMenu> initMenus() {
        List<MetaMenu> menus = new ArrayList<>();
        // Revisions are read inside the records themselves; this table is the administrative view
        // of them, so it sits next to the other logs under System Management.
        MetaMenu manager = EruptUpmsAutoConfiguration.managerMenu();
        menus.add(manager);
        menus.add(MetaMenu.createEruptClassMenu(EruptRecordRevision.class, manager, 86));
        return menus;
    }
}
