package xyz.erupt.upms;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import xyz.erupt.core.annotation.EruptScan;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.constant.MenuStatus;
import xyz.erupt.core.constant.MenuTypeEnum;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.module.ModuleGroup;
import xyz.erupt.core.module.ModuleInfo;
import xyz.erupt.upms.controller.EruptModuleController;
import xyz.erupt.tpl.service.EruptTplService;
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

    public static final String ERUPT_UPMS = "erupt-upms";

    public static final String MANAGER_MENU = "$manager";

    public static final String INTEGRATION_MENU = "$integration";

    public static final String AUDIT_MENU = "$audit";

    /**
     * The System Management root. A module whose menu is an administrative view rather than a
     * feature of its own hangs its table here instead of adding another root: menus are persisted
     * by code, so declaring this same menu again resolves to the one row whichever module happens
     * to load first — build it through this method so the two declarations cannot drift apart.
     */
    public static MetaMenu managerMenu() {
        return MetaMenu.createRootMenu(MANAGER_MENU, "System Management", "fa fa-cogs", 10);
    }

    /**
     * The Integrations root: what connects erupt to the outside — open API keys, single sign-on,
     * webhooks, share links. Shared like {@link #managerMenu()}; build it through this method.
     */
    public static MetaMenu integrationMenu() {
        return MetaMenu.createRootMenu(INTEGRATION_MENU, "Integrations", "fa fa-plug", 12);
    }

    /**
     * The Audit &amp; Logs root: who is online and what happened — login, operation, revision and
     * comment trails, the system log. Shared like {@link #managerMenu()}; build it through this method.
     */
    public static MetaMenu auditMenu() {
        return MetaMenu.createRootMenu(AUDIT_MENU, "Audit & Logs", "fa fa-clipboard-list", 14);
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name(ERUPT_UPMS).group(ModuleGroup.PLATFORM)
                .description("Users, roles, organizations, menus and permissions of the application")
                .docs(ModuleInfo.eruptDocs("modules/erupt-upms/")).build();
    }

    @Override
    public List<MetaMenu> initMenus() {
        List<MetaMenu> menus = new ArrayList<>();
        MetaMenu manager = managerMenu(), integration = integrationMenu(), audit = auditMenu();
        menus.add(manager);
        menus.add(integration);
        menus.add(audit);
        menus.add(MetaMenu.createSimpleMenu("$home", "Home", "/", null, 0, "fa fa-home", MenuTypeEnum.ROUTER.getCode(), MenuStatus.OPEN));
        // System Management: people, permissions and the platform itself
        menus.add(MetaMenu.createEruptClassMenu(EruptMenu.class, manager, 0, MenuTypeEnum.TREE));
        menus.add(MetaMenu.createEruptClassMenu(EruptRole.class, manager, 10));
        menus.add(MetaMenu.createEruptClassMenu(EruptOrg.class, manager, 20, MenuTypeEnum.TREE));
        menus.add(MetaMenu.createEruptClassMenu(EruptPost.class, manager, 30, MenuTypeEnum.TREE));
        menus.add(MetaMenu.createEruptClassMenu(EruptUser.class, manager, 40));
        menus.add(MetaMenu.createEruptClassMenu(EruptDict.class, manager, 50));
        menus.add(MetaMenu.createEruptClassMenu(EruptDictItem.class, manager, 51, MenuStatus.HIDE));
        menus.add(MetaMenu.createEruptClassMenu(EruptSiteSetting.class, manager, 60, MenuTypeEnum.FORM));
        menus.add(MetaMenu.createSimpleMenu("$modules", "Module Center", EruptModuleController.PAGE, manager, 70, MenuTypeEnum.ROUTER.getCode()));
        // Integrations: erupt-sso 20, erupt-webhook 30, erupt-share 40
        menus.add(MetaMenu.createEruptClassMenu(EruptOpenApi.class, integration, 10));
        // Audit & Logs: erupt-revision 40, erupt-comment 50
        menus.add(MetaMenu.createEruptClassMenu(EruptOnline.class, audit, 10));
        menus.add(MetaMenu.createEruptClassMenu(EruptLoginLog.class, audit, 20));
        menus.add(MetaMenu.createEruptClassMenu(EruptOperateLog.class, audit, 30));
        menus.add(MetaMenu.createSimpleMenu(EruptConst.ERUPT_LOG, "System Log", EruptConst.ERUPT_LOG, audit, 60, EruptTplService.TPL));
        return menus;
    }

}
