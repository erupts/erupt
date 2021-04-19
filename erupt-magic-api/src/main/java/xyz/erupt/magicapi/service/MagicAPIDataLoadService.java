package xyz.erupt.magicapi.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.ssssssss.magicapi.interceptor.Authorization;
import xyz.erupt.core.util.ProjectUtil;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.magicapi.action.MagicApiTpl;
import xyz.erupt.tpl.service.EruptTplService;
import xyz.erupt.upms.enums.MenuStatus;
import xyz.erupt.upms.enums.MenuTypeEnum;
import xyz.erupt.upms.model.EruptMenu;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Order
@Slf4j
public class MagicAPIDataLoadService implements CommandLineRunner {

    public static final String MAGIC_API_MENU_PREFIX = "ERUPT_MAGIC_";

    @Resource
    private EruptDao eruptDao;

    @Transactional
    @Override
    public void run(String... args) {
        String menuKey = "magic-api";
        new ProjectUtil().projectStartLoaded(menuKey, bool -> {
            if (bool) {
                AtomicInteger sort = new AtomicInteger(80);
                EruptMenu eruptMenu = eruptDao.persistIfNotExist(EruptMenu.class,
                        new EruptMenu(menuKey, "接口配置", EruptTplService.TPL,
                                MagicApiTpl.MAGIC_API_PERMISSION, MenuStatus.OPEN.getValue(),
                                sort.getAndAdd(10), "fa fa-cube", null),
                        "code", menuKey);
                Map<Authorization, String> menus = new LinkedHashMap<>();
                menus.put(Authorization.VIEW, "查看详情");
                menus.put(Authorization.SAVE, "保存");
                menus.put(Authorization.DELETE, "删除");
                menus.put(Authorization.UPLOAD, "上传");
                menus.put(Authorization.DOWNLOAD, "导出");
                menus.put(Authorization.DATASOURCE_VIEW, "数据源查看详情");
                menus.put(Authorization.DATASOURCE_SAVE, "数据源保存");
                menus.put(Authorization.DATASOURCE_DELETE, "数据源删除");
                menus.forEach((key, value) -> {
                    String menuCode = menuKey + "-" + key.name().toLowerCase();
                    eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu(menuCode, value,
                            MenuTypeEnum.BUTTON.getCode(),
                            MAGIC_API_MENU_PREFIX + key.name(),
                            MenuStatus.HIDE.getValue(), sort.getAndAdd(10),
                            null, eruptMenu), "code", menuCode);
                });
            }
        });
    }
}
