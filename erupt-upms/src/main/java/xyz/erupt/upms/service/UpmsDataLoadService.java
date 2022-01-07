package xyz.erupt.upms.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.util.MD5Util;
import xyz.erupt.core.util.ProjectUtil;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.model.EruptUser;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.Date;

/**
 * @author YuePeng
 * date 2019-07-15.
 */
@Service
@Order
@Slf4j
public class UpmsDataLoadService implements CommandLineRunner {

    @Resource
    private EruptDao eruptDao;

    public static final String DEFAULT_ACCOUNT = "erupt";

    @Transactional
    @Override
    public void run(String... args) {
        EruptModuleInvoke.invoke(module -> {
//            if (null != module.menus()) new ProjectUtil().projectStartLoaded(module.info().getName(), first -> {
//                if (first) {
//                    for (MetaMenu metaMenu : module.menus()) {
//                        metaMenu.setId(
//                                eruptDao.persistIfNotExist(EruptMenu.class,
//                                        EruptMenu.fromMetaMenu(metaMenu), EruptMenu.CODE, metaMenu.getCode()
//                                ).getId()
//                        );
//                    }
//                }
//            });
        });
        new ProjectUtil().projectStartLoaded("erupt-upms-user", first -> {
            if (first) {
                //用户
                if (eruptDao.queryEntityList(EruptUser.class, "isAdmin = true").size() <= 0) {
                    EruptUser eruptUser = new EruptUser();
                    eruptUser.setIsAdmin(true);
                    eruptUser.setIsMd5(true);
                    eruptUser.setStatus(true);
                    eruptUser.setCreateTime(new Date());
                    eruptUser.setAccount(DEFAULT_ACCOUNT);
                    eruptUser.setPassword(MD5Util.digest(DEFAULT_ACCOUNT));
                    eruptUser.setName(DEFAULT_ACCOUNT);
                    eruptUser.setResetPwdTime(new Date());
                    eruptDao.persistIfNotExist(EruptUser.class, eruptUser, "account", eruptUser.getAccount());
                }
            } else {
                EruptUser eruptUser = eruptDao.queryEntity(EruptUser.class, "account = '" + DEFAULT_ACCOUNT + "'", null);
                if (null != eruptUser) {
                    String password = eruptUser.getPassword();
                    if (!eruptUser.getIsMd5()) {
                        password = MD5Util.digest(eruptUser.getPassword());
                    }
                    if (MD5Util.digest(DEFAULT_ACCOUNT).equals(password)) {
                        log.warn("正在使用框架默认用户名密码，为了您的系统安全请尽快修改！");
                    }
                }
            }
        });
    }

}
