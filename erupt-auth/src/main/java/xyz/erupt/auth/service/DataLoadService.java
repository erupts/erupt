package xyz.erupt.auth.service;

import lombok.extern.java.Log;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import xyz.erupt.auth.model.EruptMenu;
import xyz.erupt.auth.model.EruptUser;
import xyz.erupt.core.util.ProjectUtil;
import xyz.erupt.eruptcommon.util.MD5Utils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

/**
 * Created by liyuepeng on 2019-07-15.
 */
@Service
@Order(10)
@Log
public class DataLoadService implements CommandLineRunner {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    @Override
    public void run(String... args) {
        new ProjectUtil().projectStartLoaded("auth", first -> {
            if (first) {
                try {
                    //用户
                    EruptUser eruptUser = new EruptUser();
                    eruptUser.setIsAdmin(true);
                    eruptUser.setIsMD5(true);
                    eruptUser.setStatus(true);
                    eruptUser.setAccount("erupt");
                    eruptUser.setPassword(MD5Utils.digest("erupt"));
                    eruptUser.setName("erupt");
                    entityManager.persist(eruptUser);
                    //菜单
                    EruptMenu eruptMenu = new EruptMenu("manager", "系统管理", null, 1, 0, "fa fa-cogs", null);
                    entityManager.persist(eruptMenu);
                    entityManager.persist(new EruptMenu(
                            "menu", "菜单维护", "/build/tree/EruptMenu", 1, 0, "fa fa-list-ul", eruptMenu
                    ));
                    entityManager.persist(new EruptMenu(
                            "user", "用户维护", "/build/table/EruptUser", 1, 10, "fa fa-user", eruptMenu
                    ));
                    entityManager.persist(new EruptMenu(
                            "role", "角色维护", "/build/table/EruptRole", 1, 20, "fa fa-users", eruptMenu
                    ));
                    entityManager.persist(new EruptMenu(
                            "dict", "字典维护", "/build/table/EruptDict", 1, 30, "fa fa-book", eruptMenu
                    ));
                } catch (Exception e) {
                    log.severe("初始化用户角色数据失败：" + e.getMessage());
                }
            }
        });
    }

}
