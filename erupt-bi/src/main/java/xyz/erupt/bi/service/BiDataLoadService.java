package xyz.erupt.bi.service;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import xyz.erupt.auth.model.EruptMenu;
import xyz.erupt.bi.model.BiFunction;
import xyz.erupt.core.util.ProjectUtil;
import xyz.erupt.tool.EruptDao;

import javax.transaction.Transactional;
import java.util.List;

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

    static String[] functions = {};

    @Transactional
    @Override
    public void run(String... args) throws Exception {
        new ProjectUtil().projectStartLoaded("bi", first -> {
            if (first) {
                EruptMenu eruptMenu = eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu("mbi", "报表维护",
                        null, 1, 20, "fa fa-table", null), "code", "mbi");
                eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu("BiDataSource", "数据源管理", "/build/table/BiDataSource"
                        , 1, 10, null, eruptMenu), "code", "BiDataSource");
                eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu("BiClassHandler", "报表处理类", "/build/table/BiClassHandler"
                        , 1, 15, null, eruptMenu), "code", "BiClassHandler");
                eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu("BiFunction", "函数管理", "/build/table/BiFunction"
                        , 1, 20, null, eruptMenu), "code", "BiFunction");
                eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu("Bi", "报表配置", "/build/table/bi"
                        , 1, 30, null, eruptMenu), "code", "Bi");
                eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu("BiChart", "图表配置", "/build/table/BiChart"
                        , 2, 100, null, eruptMenu), "code", "BiChart");
            }
        });
        flushFunction();
        log.info("Erupt bi initialization complete");
    }

    public void flushFunction() {
        List<Object[]> list = eruptDao.queryObjectList(BiFunction.class, null, null, "jsFunction");
        functions = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            functions[i] = ((Object) list.get(i)).toString();
        }
    }

}
