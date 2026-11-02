package xyz.erupt.test.upms;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionTemplate;
import xyz.erupt.core.constant.MenuStatus;
import xyz.erupt.core.constant.MenuTypeEnum;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.test.EruptApplicationTests;
import xyz.erupt.test.model.erupt.OrderByModel;
import xyz.erupt.upms.model.EruptMenu;
import xyz.erupt.upms.model.EruptModuleSeed;
import xyz.erupt.upms.service.EruptMenuSeeder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The menu seeder: skipped while a module's definition is unchanged, a three-way merge per field
 * when it changes, retired menus hidden, function buttons derived from the power.
 */
class MenuSeederTest extends EruptApplicationTests {

    private static final String MODULE = "seed-test";

    private static final String ROOT = "seed-root";

    private static final String PAGE = OrderByModel.class.getSimpleName();

    private static final String EXTRA = "seed-extra";

    @Resource
    private EruptMenuSeeder seeder;

    @Resource
    private TransactionTemplate transactionTemplate;

    private final AtomicInteger initRuns = new AtomicInteger();

    @BeforeEach
    @AfterEach
    void cleanUp() {
        transactionTemplate.executeWithoutResult(status -> {
            eruptDao.lambdaQuery(EruptMenu.class).like(EruptMenu::getCode, PAGE + "@").delete();
            eruptDao.lambdaQuery(EruptMenu.class).in(EruptMenu::getCode, List.of(PAGE, EXTRA)).delete();
            eruptDao.lambdaQuery(EruptMenu.class).eq(EruptMenu::getCode, ROOT).delete();
            eruptDao.lambdaQuery(EruptModuleSeed.class).eq(EruptModuleSeed::getModule, MODULE).delete();
        });
        initRuns.set(0);
    }

    private List<MetaMenu> menus(String pageName, String pageIcon, boolean extra) {
        MetaMenu root = MetaMenu.createRootMenu(ROOT, "Seed Root", "fa fa-folder", 900);
        MetaMenu page = MetaMenu.createEruptClassMenu(OrderByModel.class, root, 10);
        page.setName(pageName);
        page.setIcon(pageIcon);
        List<MetaMenu> list = new ArrayList<>(List.of(root, page));
        if (extra) list.add(MetaMenu.createSimpleMenu(EXTRA, "Extra", "/seed-extra", root, 20, MenuTypeEnum.ROUTER.getCode()));
        return list;
    }

    private boolean seed(List<MetaMenu> menus) {
        return seeder.seed(MODULE, menus, Set.of(), false, initRuns::incrementAndGet);
    }

    private EruptMenu menu(String code) {
        return eruptDao.lambdaQuery(EruptMenu.class).eq(EruptMenu::getCode, code).one();
    }

    private void edit(String code, Consumer<EruptMenu> change) {
        transactionTemplate.executeWithoutResult(status -> change.accept(this.menu(code)));
    }

    @Test
    void seedsOnceAndSkipsWhileTheDefinitionIsUnchanged() {
        assertTrue(this.seed(this.menus("Orders", "fa fa-a", true)));
        assertEquals(1, initRuns.get(), "initFun runs before the first seeding");
        assertEquals(ROOT, this.menu(PAGE).getParentMenu().getCode());
        EruptMenu add = this.menu(PAGE + "@ADD");
        assertNotNull(add, "a table menu brings its function buttons");
        assertEquals(PAGE, add.getParentMenu().getCode());
        assertNull(this.menu(PAGE + "@IMPORT"), "no button for a power the model does not grant");

        assertFalse(this.seed(this.menus("Orders", "fa fa-a", true)), "same definition, nothing to do");
        transactionTemplate.executeWithoutResult(status -> eruptDao.delete(this.menu(EXTRA)));
        assertFalse(this.seed(this.menus("Orders", "fa fa-a", true)));
        assertNull(this.menu(EXTRA), "a menu the admin deleted stays deleted while the definition is unchanged");
        assertEquals(1, initRuns.get());

        assertTrue(seeder.seed(MODULE, this.menus("Orders", "fa fa-a", true), Set.of(), true, initRuns::incrementAndGet));
        assertEquals(2, initRuns.get(), "forced: initFun runs again");
        assertNotNull(this.menu(EXTRA), "and a missing menu is created");
    }

    @Test
    void aChangedDefinitionReachesTheFieldsNobodyEdited() {
        this.seed(this.menus("Orders", "fa fa-a", true));
        this.edit(PAGE, row -> row.setName("My Orders"));

        assertTrue(this.seed(this.menus("Orders v2", "fa fa-b", true)), "no version bump needed: the definition changed");
        EruptMenu page = this.menu(PAGE);
        assertEquals("My Orders", page.getName(), "the admin's edit is kept");
        assertEquals("fa fa-b", page.getIcon(), "an untouched field takes the new definition");
        assertEquals(1, initRuns.get(), "initFun is once per database");
    }

    @Test
    void aDroppedMenuIsDisabledAndComesBackWhenDeclaredAgain() {
        this.seed(this.menus("Orders", "fa fa-a", true));
        this.seed(this.menus("Orders", "fa fa-a", false));
        assertEquals(MenuStatus.DISABLE.getValue(), this.menu(EXTRA).getStatus(), "disabled, so it grants nothing, but not deleted");
        this.seed(this.menus("Orders", "fa fa-a", true));
        assertEquals(MenuStatus.OPEN.getValue(), this.menu(EXTRA).getStatus());
    }

    @Test
    void aMenuAnotherModuleDeclaresIsNotHidden() {
        this.seed(this.menus("Orders", "fa fa-a", true));
        seeder.seed(MODULE, this.menus("Orders", "fa fa-a", false), Set.of(EXTRA), false, initRuns::incrementAndGet);
        assertEquals(MenuStatus.OPEN.getValue(), this.menu(EXTRA).getStatus());
    }

    @Test
    void afterPlacesAMenuNextToItsSibling() {
        this.seed(this.menus("Orders", "fa fa-a", false));
        this.edit(PAGE, row -> row.setSort(55));
        List<MetaMenu> menus = this.menus("Orders", "fa fa-a", false);
        menus.add(MetaMenu.createSimpleMenu(EXTRA, "Extra", "/seed-extra", menus.get(0), null, MenuTypeEnum.ROUTER.getCode()).after(PAGE));
        this.seed(menus);
        assertEquals(56, this.menu(EXTRA).getSort(), "right after the sibling, where the admin put it");
    }

}
