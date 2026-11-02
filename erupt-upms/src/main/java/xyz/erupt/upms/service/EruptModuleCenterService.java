package xyz.erupt.upms.service;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import xyz.erupt.core.constant.MenuTypeEnum;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.module.ModuleGroup;
import xyz.erupt.core.module.ModuleInfo;
import xyz.erupt.upms.EruptUpmsAutoConfiguration;
import xyz.erupt.upms.vo.ModuleVo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The module center: every registered module with what it is for, where its documentation is and
 * a switch for its menus, so a newcomer can read the installation as a map and an administrator
 * can hide what the installation does not use.
 *
 * @author YuePeng
 * date 2026-11-01
 */
@Service
public class EruptModuleCenterService {

    // switching these off would take the menu that switches them back on with them
    private static final Set<String> ESSENTIAL = Set.of(EruptUpmsAutoConfiguration.ERUPT_UPMS);

    @Resource
    private EruptMenuSeeder menuSeeder;

    private volatile Map<String, ModuleGroup> rootGroups;

    public List<ModuleVo> modules() {
        List<ModuleVo> modules = new ArrayList<>();
        EruptModuleInvoke.invoke(module -> {
            ModuleInfo info = module.info();
            ModuleVo vo = new ModuleVo();
            vo.setName(info.getName());
            vo.setGroup(info.getGroup());
            vo.setDocs(info.getDocs());
            if (null != info.getDescription()) vo.setDescription(I18nTranslate.$translate(info.getDescription()));
            List<MetaMenu> menus = module.initMenus();
            if (null != menus && !menus.isEmpty()) {
                vo.setMenus((int) menus.stream().filter(it -> !MenuTypeEnum.BUTTON.getCode().equals(it.getType())
                        && !MenuTypeEnum.API.getCode().equals(it.getType())).count());
                if (!ESSENTIAL.contains(info.getName())) vo.setEnabled(!menuSeeder.disabled(info.getName()));
            }
            modules.add(vo);
        });
        return modules;
    }

    /** Switches a module's menus on or off. */
    public void enabled(String name, boolean value) {
        ModuleVo module = this.modules().stream().filter(it -> name.equals(it.getName())).findFirst()
                .orElseThrow(() -> new EruptWebApiRuntimeException(I18nTranslate.$translate("upms.module_unknown")));
        if (null == module.getEnabled()) throw new EruptWebApiRuntimeException(I18nTranslate.$translate("upms.module_fixed"));
        if (value) {
            menuSeeder.enable(name);
        } else {
            menuSeeder.disable(name);
        }
    }

    /** The group of the module that declares a root menu, by menu code; null for a menu no module declares. */
    public ModuleGroup groupOf(String menuCode) {
        if (null == rootGroups) {
            Map<String, ModuleGroup> groups = new HashMap<>();
            EruptModuleInvoke.invoke(module -> {
                List<MetaMenu> menus = module.initMenus();
                if (null == menus || null == module.info().getGroup()) return;
                for (MetaMenu menu : menus) {
                    if (null == menu.getParentMenu()) groups.putIfAbsent(menu.getCode(), module.info().getGroup());
                }
            });
            rootGroups = groups;
        }
        return rootGroups.get(menuCode);
    }

}
