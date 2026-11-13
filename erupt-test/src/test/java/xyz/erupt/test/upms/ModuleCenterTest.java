package xyz.erupt.test.upms;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import xyz.erupt.core.constant.MenuStatus;
import xyz.erupt.core.module.ModuleGroup;
import xyz.erupt.test.EruptApplicationTests;
import xyz.erupt.upms.EruptUpmsAutoConfiguration;
import xyz.erupt.upms.model.EruptMenu;
import xyz.erupt.upms.service.EruptMenuSeeder;
import xyz.erupt.upms.service.EruptModuleCenterService;
import xyz.erupt.upms.vo.ModuleVo;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The module center: every module listed with its group, and a module switched off keeps its own
 * menus disabled through a restart until it is switched on again.
 */
class ModuleCenterTest extends EruptApplicationTests {

    private static final String JOB = "erupt-job";

    private static final List<String> JOB_MENUS = List.of("$job", "EruptJob", "EruptJobLog", "EruptMail");

    @Resource
    private EruptModuleCenterService moduleCenter;

    @Resource
    private EruptMenuSeeder seeder;

    @AfterEach
    void switchBackOn() {
        if (seeder.disabled(JOB)) moduleCenter.enabled(JOB, true);
    }

    private ModuleVo module(String name) {
        return moduleCenter.modules().stream().filter(it -> name.equals(it.getName())).findFirst().orElseThrow();
    }

    private Integer status(String code) {
        return eruptDao.lambdaQuery(EruptMenu.class).eq(EruptMenu::getCode, code).one().getStatus();
    }

    @Test
    void everyModuleIsListedWithItsGroupAndSwitch() {
        ModuleVo job = module(JOB);
        assertEquals(ModuleGroup.OPS.title(), job.getGroup());
        assertNotNull(job.getDescription());
        assertNotNull(job.getDocs());
        assertEquals(4, job.getMenus(), "the root and three tables, buttons left out");
        assertEquals(Boolean.TRUE, job.getEnabled());
        assertNull(module(EruptUpmsAutoConfiguration.ERUPT_UPMS).getEnabled(), "upms holds the switch itself and cannot be switched off");
        assertEquals(ModuleGroup.OPS, moduleCenter.groupOf("$job"));
        assertNull(moduleCenter.groupOf("no-such-root"));
    }

    @Test
    void aModuleSwitchedOffKeepsItsMenusDisabledUntilSwitchedOn() {
        moduleCenter.enabled(JOB, false);
        assertEquals(Boolean.FALSE, module(JOB).getEnabled());
        for (String code : JOB_MENUS) assertEquals(MenuStatus.DISABLE.getValue(), status(code), code);

        seeder.seedAll();
        for (String code : JOB_MENUS) assertEquals(MenuStatus.DISABLE.getValue(), status(code), code + " stays disabled through a restart");

        moduleCenter.enabled(JOB, true);
        assertEquals(Boolean.TRUE, module(JOB).getEnabled());
        for (String code : JOB_MENUS) assertEquals(MenuStatus.OPEN.getValue(), status(code), code);
    }

    @Test
    void upmsCannotBeSwitchedOff() {
        assertThrows(RuntimeException.class, () -> moduleCenter.enabled(EruptUpmsAutoConfiguration.ERUPT_UPMS, false));
        assertThrows(RuntimeException.class, () -> moduleCenter.enabled("no-such-module", false));
    }

    @Test
    void aCustomGroupFormsItsOwnTitledSection() {
        // the same title is the same group
        assertEquals(ModuleGroup.of("CRM"), ModuleGroup.of("CRM"));
        // no translation row: the title is its own fallback
        assertEquals("CRM", ModuleGroup.of("CRM").title());
        // custom groups list behind the built-ins, alphabetically among themselves
        assertTrue(ModuleGroup.OPS.compareTo(ModuleGroup.of("CRM")) < 0);
        assertTrue(ModuleGroup.of("Zulu").compareTo(ModuleGroup.of("Alpha")) > 0);
    }

}
