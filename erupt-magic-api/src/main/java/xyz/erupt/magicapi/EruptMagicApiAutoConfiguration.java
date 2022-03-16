package xyz.erupt.magicapi;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.ssssssss.magicapi.core.interceptor.Authorization;
import xyz.erupt.core.annotation.EruptScan;
import xyz.erupt.core.constant.MenuTypeEnum;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.module.ModuleInfo;
import xyz.erupt.magicapi.action.MagicApiTpl;
import xyz.erupt.tpl.service.EruptTplService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author YuePeng
 * date 2021/3/28 18:51
 */
@Configuration
@ComponentScan
@EruptScan
public class EruptMagicApiAutoConfiguration implements EruptModule {

    public static final String MAGIC_API_MENU_PREFIX = "ERUPT_MAGIC_";

    public static final String DATASOURCE = "DATASOURCE";

    public static final String FUNCTION = "FUNCTION";

    static {
        EruptModuleInvoke.addEruptModule(EruptMagicApiAutoConfiguration.class);
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name("erupt-magic-api").build();
    }

    @Override
    public List<MetaMenu> initMenus() {
        String menuKey = "magic-api";
        Map<Authorization, String> menus = new LinkedHashMap<>();
        menus.put(Authorization.SAVE, "保存");
        menus.put(Authorization.VIEW, "查看");
        menus.put(Authorization.DELETE, "删除");
        menus.put(Authorization.DOWNLOAD, "导出");
        menus.put(Authorization.UPLOAD, "上传");
        menus.put(Authorization.PUSH, "远程推送");
        menus.put(Authorization.LOCK, "锁定");
        menus.put(Authorization.UNLOCK, "解锁");
        AtomicInteger sort = new AtomicInteger();
        List<MetaMenu> metaMenus = new ArrayList<>();
        metaMenus.add(MetaMenu.createSimpleMenu(menuKey, "接口配置", MagicApiTpl.MAGIC_API_PERMISSION, null, 50, EruptTplService.TPL));
        metaMenus.add(MetaMenu.createSimpleMenu(menuKey + "-" + FUNCTION.toLowerCase(), "函数", MAGIC_API_MENU_PREFIX + FUNCTION.toUpperCase(), metaMenus.get(0), sort.addAndGet(10), MenuTypeEnum.BUTTON.getCode()));
        metaMenus.add(MetaMenu.createSimpleMenu(menuKey + "-" + DATASOURCE.toLowerCase(), "数据源", MAGIC_API_MENU_PREFIX + DATASOURCE.toUpperCase(), metaMenus.get(0), sort.addAndGet(10), MenuTypeEnum.BUTTON.getCode()));
        menus.forEach((key, value) -> metaMenus.add(MetaMenu.createSimpleMenu(
                menuKey + "-" + key.name().toLowerCase(), value,
                MAGIC_API_MENU_PREFIX + key.name(),
                metaMenus.get(0), sort.addAndGet(10),
                MenuTypeEnum.BUTTON.getCode()
        )));
        return metaMenus;
    }
}
