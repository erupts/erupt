package xyz.erupt.upms.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.constant.MenuStatus;
import xyz.erupt.core.constant.MenuTypeEnum;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.enums.EruptFunPermissions;
import xyz.erupt.upms.model.EruptMenu;
import xyz.erupt.upms.model.EruptModuleSeed;
import xyz.erupt.upms.util.UPMSUtil;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Function;

/**
 * Brings the menu table in line with what each module's {@link EruptModule#initMenus()} declares,
 * on every start, at the cost of one query when nothing changed.
 * <ul>
 *     <li>A module is skipped while the fingerprint of its definitions matches the one stored in
 *     {@code e_upms_module}; a changed definition is reconciled whatever the erupt version.</li>
 *     <li>Reconciling is a three-way merge per field: a menu field still holding what the module
 *     wrote last time takes the new definition, a field an admin edited keeps the edit. A menu the
 *     database never had is created; one the admin deleted while the definition was unchanged is
 *     left deleted.</li>
 *     <li>The function buttons of a table menu (add, edit, ...) are part of the definition, so a
 *     power switched on later gains its button.</li>
 *     <li>A menu the module no longer declares is disabled, not deleted, unless another module still
 *     declares it: it leaves the tree and grants nothing (a hidden menu would still grant access).
 *     Declaring it again opens it again.</li>
 *     <li>Instances starting together serialize on the module's row, so nothing is inserted twice.</li>
 * </ul>
 * A menu declared with {@link MetaMenu#after(String)} is placed right after that sibling.
 *
 * @author YuePeng
 */
@Slf4j
@Service
public class EruptMenuSeeder {

    // part of every fingerprint: a change of how menus are derived re-runs every module once
    private static final String ALGORITHM = "menu-seed/1";

    private static final Gson GSON = GsonFactory.getGson();

    @Resource
    private EruptDao eruptDao;

    @Resource
    private EruptModuleStore moduleStore;

    /** Seeds every registered module that declares menus. */
    @Transactional
    public void seedAll() {
        Map<String, EruptModule> modules = new LinkedHashMap<>();
        Map<String, List<MetaMenu>> definitions = new LinkedHashMap<>();
        Map<String, Set<String>> owners = new HashMap<>();
        EruptModuleInvoke.invoke(module -> {
            List<MetaMenu> menus = module.initMenus();
            if (null == menus) return;
            String name = module.info().getName();
            List<MetaMenu> expanded = expand(menus);
            modules.put(name, module);
            definitions.put(name, expanded);
            for (MetaMenu menu : expanded) owners.computeIfAbsent(menu.getCode(), it -> new HashSet<>()).add(name);
        });
        Map<String, EruptModuleSeed> known = new HashMap<>();
        for (EruptModuleSeed seed : eruptDao.lambdaQuery(EruptModuleSeed.class).list()) known.put(seed.getModule(), seed);
        definitions.forEach((name, menus) -> {
            Set<String> shared = new HashSet<>();
            owners.forEach((code, by) -> {
                if (by.size() > 1 || !by.contains(name)) shared.add(code);
            });
            this.seed(name, menus, shared, false, modules.get(name)::initFun, known.get(name));
        });
    }

    /**
     * Seeds one module's menus.
     *
     * @param shared  menu codes another module declares too: never hidden on this module's behalf
     * @param force   reconcile and run initFun even when the fingerprint matches
     * @param initFun the module's one-off initialisation, run before its first seeding
     * @return whether the menus were reconciled (false: the fingerprint matched)
     */
    @Transactional
    public boolean seed(String module, List<MetaMenu> menus, Set<String> shared, boolean force, Runnable initFun) {
        return this.seed(module, expand(menus), shared, force, initFun,
                moduleStore.find(module));
    }

    private boolean seed(String module, List<MetaMenu> definitions, Set<String> shared, boolean force, Runnable initFun, EruptModuleSeed known) {
        String fingerprint = fingerprint(definitions);
        if (!force && null != known && fingerprint.equals(known.getFingerprint())) return false;
        EruptModuleSeed seed = moduleStore.lock(module, known);
        // another instance may have done the work while this one waited for the lock
        if (!force && fingerprint.equals(seed.getFingerprint())) return false;
        boolean first = null == seed.getSeededAt();
        // an install from before e_upms_module marked its first run with a file instead
        if (force || (first && !moduleStore.legacyMarked(module))) initFun.run();
        Map<String, Snap> snapshot = this.reconcile(definitions, parse(seed.getSnapshot()), shared);
        seed.setFingerprint(fingerprint);
        seed.setSnapshot(GSON.toJson(snapshot));
        moduleStore.markSeeded(seed);
        log.info("Menus of '{}' {}", module, first ? "seeded" : "reconciled with their changed definition");
        return true;
    }

    // ---------- reconcile ----------

    private Map<String, Snap> reconcile(List<MetaMenu> definitions, Map<String, Snap> before, Set<String> shared) {
        Set<String> codes = new HashSet<>(before.keySet());
        for (MetaMenu menu : definitions) {
            codes.add(menu.getCode());
            if (null != menu.getParentMenu()) codes.add(menu.getParentMenu().getCode());
            if (null != menu.getAfter()) codes.add(menu.getAfter());
        }
        Map<String, EruptMenu> rows = new HashMap<>();
        for (EruptMenu row : eruptDao.lambdaQuery(EruptMenu.class).in(EruptMenu::getCode, codes).list()) rows.put(row.getCode(), row);
        Map<String, Snap> after = new LinkedHashMap<>();
        for (MetaMenu menu : definitions) {
            Snap target = Snap.of(menu, rows);
            EruptMenu row = rows.get(menu.getCode());
            Snap last = before.get(menu.getCode());
            if (null == row) {
                row = new EruptMenu(menu.getCode(), target.name, target.type, target.value, target.status, target.sort, target.icon, this.menu(target.parent, rows));
                eruptDao.persist(row);
                rows.put(row.getCode(), row);
            } else if (null == last) {
                // first seen by the seeder: whatever is there was put there before it, keep it
                if (!target.equals(Snap.of(row))) log.info("Menu '{}' differs from its module's definition and is kept as it is", menu.getCode());
            } else {
                this.merge(row, last, target, rows);
            }
            menu.setId(row.getId());
            after.put(menu.getCode(), target);
        }
        before.forEach((code, last) -> {
            if (after.containsKey(code) || shared.contains(code)) return;
            EruptMenu row = rows.get(code);
            Snap retired = last.withStatus(MenuStatus.DISABLE.getValue());
            if (null != row && Objects.equals(row.getStatus(), last.status)) {
                if (!Objects.equals(row.getStatus(), retired.status)) log.info("Menu '{}' is no longer declared by its module and is disabled", code);
                row.setStatus(retired.status);
                after.put(code, retired);
            } else {
                after.put(code, last);
            }
        });
        return after;
    }

    // Per field: still what the module wrote last time means nobody touched it, so take the new definition
    private void merge(EruptMenu row, Snap last, Snap target, Map<String, EruptMenu> rows) {
        if (Objects.equals(row.getName(), last.name)) row.setName(target.name);
        if (Objects.equals(row.getType(), last.type)) row.setType(target.type);
        if (Objects.equals(row.getValue(), last.value)) row.setValue(target.value);
        if (Objects.equals(row.getSort(), last.sort)) row.setSort(target.sort);
        if (Objects.equals(row.getIcon(), last.icon)) row.setIcon(target.icon);
        if (Objects.equals(row.getStatus(), last.status)) row.setStatus(target.status);
        String parent = null == row.getParentMenu() ? null : row.getParentMenu().getCode();
        if (Objects.equals(parent, last.parent) && !Objects.equals(parent, target.parent)) row.setParentMenu(this.menu(target.parent, rows));
    }

    private EruptMenu menu(String code, Map<String, EruptMenu> rows) {
        if (null == code) return null;
        return rows.computeIfAbsent(code, it -> eruptDao.lambdaQuery(EruptMenu.class).eq(EruptMenu::getCode, it).one());
    }

    // ---------- definitions ----------

    // The declared menus plus the function buttons of every table menu, as the power allows them
    static List<MetaMenu> expand(List<MetaMenu> menus) {
        List<MetaMenu> all = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (MetaMenu menu : menus) {
            if (!seen.add(menu.getCode())) continue;
            all.add(menu);
            if (null == menu.getType() || null == menu.getValue()) continue;
            EruptFunPermissions[] permissions = EruptFunPermissions.byMenuType(menu.getType());
            EruptModel model = null == permissions ? null : EruptCoreService.getErupt(menu.getValue());
            if (null == model) continue;
            Power power = model.getErupt().power();
            int sort = 0;
            for (EruptFunPermissions permission : permissions) {
                if (!permission.verifyPower(power)) continue;
                String code = UPMSUtil.getEruptFunPermissionsCode(menu.getValue(), permission);
                if (!seen.add(code)) continue;
                all.add(MetaMenu.createSimpleMenu(code, permission.getName(), code, menu, sort += 10, MenuTypeEnum.BUTTON.getCode()));
            }
        }
        return all;
    }

    static String fingerprint(List<MetaMenu> definitions) {
        StringBuilder text = new StringBuilder(ALGORITHM);
        for (MetaMenu menu : definitions) {
            text.append('\n').append(String.join("\u0001", menu.getCode(), menu.getName(), menu.getType(), menu.getValue(),
                    String.valueOf(menu.getSort()), menu.getIcon(), String.valueOf(Snap.status(menu)),
                    null == menu.getParentMenu() ? null : menu.getParentMenu().getCode(), menu.getAfter()));
        }
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(text.toString().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Map<String, Snap> parse(String snapshot) {
        if (null == snapshot || snapshot.isEmpty()) return new LinkedHashMap<>();
        Map<String, Snap> map = GSON.fromJson(snapshot, new TypeToken<LinkedHashMap<String, Snap>>() {
        }.getType());
        return null == map ? new LinkedHashMap<>() : map;
    }

    /**
     * The fields of one menu as the module defines them (or as a row holds them). {@code parent}
     * is the parent's code. A plain class rather than a record, for the Gson round trip.
     */
    static final class Snap {

        String name;
        String type;
        String value;
        Integer sort;
        String icon;
        Integer status;
        String parent;

        static Integer status(MetaMenu menu) {
            return (null == menu.getStatus() ? MenuStatus.OPEN : menu.getStatus()).getValue();
        }

        static Snap of(MetaMenu menu, Map<String, EruptMenu> rows) {
            Snap snap = new Snap();
            snap.name = menu.getName();
            snap.type = menu.getType();
            snap.value = menu.getValue();
            snap.sort = menu.getSort();
            snap.icon = menu.getIcon();
            snap.status = status(menu);
            snap.parent = null == menu.getParentMenu() ? null : menu.getParentMenu().getCode();
            // placed after a sibling: one past its order, wherever the admin has moved it
            Optional.ofNullable(menu.getAfter()).map(rows::get).map(EruptMenu::getSort).ifPresent(it -> snap.sort = it + 1);
            return snap;
        }

        static Snap of(EruptMenu row) {
            Snap snap = new Snap();
            snap.name = row.getName();
            snap.type = row.getType();
            snap.value = row.getValue();
            snap.sort = row.getSort();
            snap.icon = row.getIcon();
            snap.status = row.getStatus();
            snap.parent = Optional.ofNullable(row.getParentMenu()).map(EruptMenu::getCode).orElse(null);
            return snap;
        }

        Snap withStatus(Integer status) {
            Snap copy = GSON.fromJson(GSON.toJson(this), Snap.class);
            copy.status = status;
            return copy;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Snap other)) return false;
            return Objects.equals(name, other.name) && Objects.equals(type, other.type) && Objects.equals(value, other.value)
                    && Objects.equals(sort, other.sort) && Objects.equals(icon, other.icon)
                    && Objects.equals(status, other.status) && Objects.equals(parent, other.parent);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, type, value, sort, icon, status, parent);
        }
    }

}
