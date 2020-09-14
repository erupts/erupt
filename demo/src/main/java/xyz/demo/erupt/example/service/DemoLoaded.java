package xyz.demo.erupt.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import xyz.erupt.auth.model.EruptMenu;
import xyz.erupt.core.dao.EruptDao;
import xyz.erupt.core.util.ProjectUtil;

import javax.transaction.Transactional;

/**
 * @author liyuepeng
 * @date 2020-09-14
 */
@Component
@Order
public class DemoLoaded implements CommandLineRunner {

    @Autowired
    private EruptDao eruptDao;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        new ProjectUtil().projectStartLoaded("demo", bool -> {
            if (bool) {
                String $code = "code";
                eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu("erupt", "Erupt通用数据管理框架", EruptMenu.PATH_LINK + "https://www.erupt.xyz",
                        new Integer(EruptMenu.OPEN), 100, "fa fa-star", null), $code, "erupt");
                eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu("doc", "使用文档", EruptMenu.PATH_LINK + "https://www.yuque.com/yuepeng/erupt",
                        new Integer(EruptMenu.OPEN), 110, "fa fa-book", null), $code, "doc");
            }
        });
    }
}
