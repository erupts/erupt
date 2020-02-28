package xyz.erupt.bi.service;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import xyz.erupt.auth.model.EruptMenu;
import xyz.erupt.core.util.ProjectUtil;
import xyz.erupt.tool.EruptDao;

import javax.transaction.Transactional;

/**
 * @author liyuepeng
 * @date 2019-07-15.
 */
@Service
@Order(10)
@Log
public class BiDataLoadService implements CommandLineRunner {

    @Autowired
    private EruptDao eruptDao;

    @Transactional
    @Override
    public void run(String... args) throws Exception {
        new ProjectUtil().projectStartLoaded("bi", first -> {
            if (first) {
                EruptMenu eruptMenu = new EruptMenu("mbi", "报表维护", null, 1, 20, "fa fa-table", null);
                eruptDao.persistIfNotExist(eruptMenu, "code", "mbi");
                eruptDao.persistIfNotExist(new EruptMenu("BiDataSource", "数据源管理", "/build/table/BiDataSource"
                        , 1, 10, null, eruptMenu), "code", "BiDataSource");
                eruptDao.persistIfNotExist(new EruptMenu("BiFunction", "报表函数", "/build/table/BiFunction"
                        , 1, 20, null, eruptMenu), "code", "BiFunction");
                eruptDao.persistIfNotExist(new EruptMenu("Bi", "报表配置", "/build/table/bi"
                        , 1, 30, null, eruptMenu), "code", "Bi");
            }
        });
    }

}
