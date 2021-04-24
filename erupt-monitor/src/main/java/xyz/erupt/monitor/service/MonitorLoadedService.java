package xyz.erupt.monitor.service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import xyz.erupt.core.util.ProjectUtil;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.monitor.model.EruptOnline;
import xyz.erupt.tpl.service.EruptTplService;
import xyz.erupt.upms.enums.MenuStatus;
import xyz.erupt.upms.enums.MenuTypeEnum;
import xyz.erupt.upms.model.EruptMenu;

import javax.annotation.Resource;
import javax.transaction.Transactional;

/**
 * @author YuePeng
 * date 2021/1/24 12:37
 */
@Component
public class MonitorLoadedService implements CommandLineRunner {

    @Resource
    private EruptDao eruptDao;

    @Override
    @Transactional
    public void run(String... args) {
        new ProjectUtil().projectStartLoaded("monitor", bool -> {
            if (bool) {
                String code = "code";
                EruptMenu eruptMenu = eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu("monitor", "系统监控", null, null,
                        MenuStatus.OPEN.getValue(), 10, "fa fa-bullseye", null), "code", "monitor");
                eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu("server.html", "服务监控", EruptTplService.TPL,
                        "server.html", MenuStatus.OPEN.getValue(), 10, null, eruptMenu), code, "server.html");
                eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu("redis.html", "缓存监控", EruptTplService.TPL,
                        "redis.html", MenuStatus.OPEN.getValue(), 20, null, eruptMenu), code, "redis.html");
                eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu(EruptOnline.class.getSimpleName(), "在线用户", MenuTypeEnum.TABLE.getCode(),
                        EruptOnline.class.getSimpleName(), MenuStatus.OPEN.getValue(), 30, null, eruptMenu), code, EruptOnline.class.getSimpleName());
            }
        });
    }
}
