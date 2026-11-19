package xyz.erupt.test.upms;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.test.EruptApplicationTests;
import xyz.erupt.upms.EruptUpmsAutoConfiguration;
import xyz.erupt.upms.model.EruptMenu;
import xyz.erupt.upms.model.EruptRole;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.model.log.EruptLoginLog;
import xyz.erupt.upms.service.EruptMenuService;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A role granting a page but not the folder above it — a folder added by a later release, as the
 * Audit &amp; Logs root was — still reaches the page: the folder is added to the user's menus, and
 * nothing else is.
 */
class MenuFolderTest extends EruptApplicationTests {

    @Resource
    private EruptDao eruptDao;

    @Resource
    private EruptMenuService eruptMenuService;

    @Test
    void grantedPageBringsItsFolderAlong() {
        EruptMenu loginLog = eruptDao.lambdaQuery(EruptMenu.class).eq(EruptMenu::getCode, EruptLoginLog.class.getSimpleName()).one();
        assertNotNull(loginLog, "the login log menu is seeded");
        EruptRole role = new EruptRole();
        role.setStatus(true);
        role.setMenus(Set.of(loginLog));
        EruptUser user = new EruptUser();
        user.setIsAdmin(false);
        user.setRoles(Set.of(role));

        List<String> codes = eruptMenuService.getUserAllMenu(user).stream().map(EruptMenu::getCode).toList();
        assertTrue(codes.contains(EruptLoginLog.class.getSimpleName()));
        assertTrue(codes.contains(EruptUpmsAutoConfiguration.AUDIT_MENU), "the folder above the granted page is added");
        assertFalse(codes.contains(EruptUpmsAutoConfiguration.MANAGER_MENU), "folders above nothing granted stay out");
        assertTrue(codes.size() == 2, "a folder grants no page: only the page and its folder, got " + codes);
    }

}
