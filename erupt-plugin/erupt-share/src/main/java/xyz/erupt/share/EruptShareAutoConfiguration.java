package xyz.erupt.share;

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
import xyz.erupt.share.model.EruptShareLink;
import xyz.erupt.upms.EruptUpmsAutoConfiguration;
import xyz.erupt.upms.prop.EruptAppProp;

import java.util.ArrayList;
import java.util.List;

/**
 * Public links. A logged-in user issues a code for one target: a form that anyone holding the
 * link may submit once or many times (a sign-up sheet, a ticket, a survey), or one of their menus
 * that anyone may open as a user holding that menu alone would (a template page, a report, a
 * dashboard, a canvas, a read-only table). The code expires, may be capped in visits, may ask a
 * password, and can be switched off at any time.
 * <p>
 * Opening a link mints an ordinary erupt session of the issuer, narrowed by
 * {@link xyz.erupt.share.service.ShareScopeGuard} to the calls the target needs: the visitor
 * acts in the issuer's name and sees exactly what the issuer could, nothing more.
 *
 * @author YuePeng
 * date 2026/10/30
 */
@Configuration
@ComponentScan
@EruptScan
@EntityScan
public class EruptShareAutoConfiguration implements EruptModule {

    static {
        EruptModuleInvoke.addEruptModule(EruptShareAutoConfiguration.class);
    }

    public static final String ERUPT_SHARE = "erupt-share";

    @Resource
    private EruptAppProp eruptAppProp;

    @PostConstruct
    public void post() {
        eruptAppProp.registerProp(ERUPT_SHARE, true);
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name(ERUPT_SHARE).description("Public share links").build();
    }

    @Override
    public List<MetaMenu> initMenus() {
        List<MetaMenu> menus = new ArrayList<>();
        MetaMenu manager = EruptUpmsAutoConfiguration.managerMenu();
        menus.add(manager);
        menus.add(MetaMenu.createEruptClassMenu(EruptShareLink.class, manager, 86));
        return menus;
    }
}
