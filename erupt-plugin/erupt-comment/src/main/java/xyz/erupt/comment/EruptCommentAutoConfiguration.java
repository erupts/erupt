package xyz.erupt.comment;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import xyz.erupt.comment.model.EruptRecordComment;
import xyz.erupt.core.annotation.EruptScan;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.module.ModuleInfo;
import xyz.erupt.upms.EruptUpmsAutoConfiguration;
import xyz.erupt.upms.prop.EruptAppProp;

import java.util.ArrayList;
import java.util.List;

/**
 * Per-record comment stream: any erupt record can carry a discussion thread, shown in the
 * record panel of the admin UI. Opt-in by adding this module; the frontend reads the
 * {@link #ERUPT_COMMENT} property to show the entry.
 *
 * @author YuePeng
 * date 2026/9/16
 */
@Configuration
@ComponentScan
@EruptScan
@EntityScan
public class EruptCommentAutoConfiguration implements EruptModule {

    static {
        EruptModuleInvoke.addEruptModule(EruptCommentAutoConfiguration.class);
    }

    public static final String ERUPT_COMMENT = "erupt-comment";

    @Resource
    private EruptAppProp eruptAppProp;

    @PostConstruct
    public void post() {
        eruptAppProp.registerProp(ERUPT_COMMENT, true);
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name(ERUPT_COMMENT).description("Record comments").build();
    }

    @Override
    public List<MetaMenu> initMenus() {
        List<MetaMenu> menus = new ArrayList<>();
        // Comments are written inside the records themselves; this table is the administrative view
        // of them, so it belongs next to the other logs under System Management rather than behind a
        // root menu of its own.
        MetaMenu manager = EruptUpmsAutoConfiguration.managerMenu();
        menus.add(manager);
        menus.add(MetaMenu.createEruptClassMenu(EruptRecordComment.class, manager, 85));
        return menus;
    }
}
