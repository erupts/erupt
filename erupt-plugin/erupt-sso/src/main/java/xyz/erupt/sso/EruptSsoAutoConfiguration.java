package xyz.erupt.sso;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import xyz.erupt.core.annotation.EruptScan;
import xyz.erupt.core.constant.MenuStatus;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.module.ModuleInfo;
import xyz.erupt.sso.model.EruptSso;
import xyz.erupt.sso.model.EruptSsoBind;
import xyz.erupt.upms.EruptUpmsAutoConfiguration;
import xyz.erupt.upms.prop.EruptAppProp;

import java.util.ArrayList;
import java.util.List;

/**
 * Delegated login over OAuth2 / OIDC.
 *
 * <p>Optional on purpose: an installation that only signs users in with a password should
 * not carry a second way into the system. The login page asks for the provider list only
 * when this module announces itself through {@link EruptAppProp#registerProp}.
 *
 * @author YuePeng
 * date 2026-09-18
 */
@Configuration
@ComponentScan
@EruptScan
@EntityScan
public class EruptSsoAutoConfiguration implements EruptModule {

    static {
        EruptModuleInvoke.addEruptModule(EruptSsoAutoConfiguration.class);
    }

    public static final String ERUPT_SSO = "erupt-sso";

    @Resource
    private EruptAppProp eruptAppProp;

    @PostConstruct
    public void post() {
        eruptAppProp.registerProp(ERUPT_SSO, true);
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name(ERUPT_SSO).description("Single sign-on").build();
    }

    @Override
    public List<MetaMenu> initMenus() {
        List<MetaMenu> menus = new ArrayList<>();
        // an administrative view of the existing system, so it hangs off the shared root
        // rather than adding one of its own
        MetaMenu manager = EruptUpmsAutoConfiguration.managerMenu();
        menus.add(manager);
        menus.add(MetaMenu.createEruptClassMenu(EruptSso.class, manager, 45));
        // Hidden: a binding is made by signing in and only ever read to investigate one.
        // Show it from Menu Management when someone has to be unbound.
        menus.add(MetaMenu.createEruptClassMenu(EruptSsoBind.class, manager, 46, MenuStatus.HIDE));
        return menus;
    }

}
