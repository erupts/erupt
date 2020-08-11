package xyz.erupt.auth.service;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import xyz.erupt.auth.model.EruptMenu;
import xyz.erupt.auth.model.EruptUser;
import xyz.erupt.auth.util.MD5Utils;
import xyz.erupt.core.dao.EruptDao;
import xyz.erupt.core.util.ProjectUtil;

import javax.transaction.Transactional;
import java.util.Date;

/**
 * @author liyuepeng
 * @date 2019-07-15.
 */
@Service
@Order(10)
@Log
public class AuthDataLoadService implements CommandLineRunner {

    @Autowired
    private EruptDao eruptDao;

    @Transactional
    @Override
    public void run(String... args) throws Exception {
        new ProjectUtil().projectStartLoaded("auth", first -> {
            if (first) {
                //用户
                EruptUser eruptUser = new EruptUser();
                eruptUser.setIsAdmin(true);
                eruptUser.setIsMd5(true);
                eruptUser.setStatus(true);
                eruptUser.setCreateTime(new Date());
                eruptUser.setAccount("erupt");
                eruptUser.setPassword(MD5Utils.digest("erupt"));
                eruptUser.setName("erupt");
                eruptDao.persistIfNotExist(EruptUser.class, eruptUser, "account", eruptUser.getAccount());
                //菜单
                EruptMenu eruptMenu = eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu("manager", "系统管理", null, 1, 0, "fa fa-cogs", null)
                        , "code", "manager");
                eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu(
                        "menu", "菜单维护", "/build/tree/EruptMenu", 1, 0, "fa fa-list-ul", eruptMenu
                ), "code", "menu");
                eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu(
                        "org", "组织维护", "/build/table/EruptOrg", 1, 5, "fa fa-users", eruptMenu
                ), "code", "org");
                eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu(
                        "user", "用户维护", "/build/table/EruptUser", 1, 10, "fa fa-user", eruptMenu
                ), "code", "user");
                eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu(
                        "role", "角色维护", "/build/table/EruptRole", 1, 20, "fa fa-users", eruptMenu
                ), "code", "role");
                eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu(
                        "dict", "字典维护", "/build/table/EruptDict", 1, 30, "fa fa-book", eruptMenu
                ), "code", "dict");
                eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu(
                        "dictItem", "字典项", "/build/tree/EruptDictItem", 2, 35, "fa fa-book", eruptMenu
                ), "code", "dictItem");
                eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu(
                        "loginLog", "登录日志", "/build/table/EruptLoginLog", 1, 40, "fa fa-book", eruptMenu
                ), "code", "loginLog");
//                eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu(
//                        "erupt", "Erupt通用数据管理框架", "/site?url=https://www.erupt.xyz", 1, 100, "fa fa-book", null
//                ), "code", "erupt");
            }
        });
    }

}
