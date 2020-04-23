package xyz.erupt.bi.service;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import xyz.erupt.auth.model.EruptMenu;
import xyz.erupt.bi.model.*;
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
                eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu(BiDataSource.class.getSimpleName(), "数据源管理", "/build/table/" + BiDataSource.class.getSimpleName()
                        , 1, 10, null, eruptMenu), "code", BiDataSource.class.getSimpleName());
                eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu(BiClassHandler.class.getSimpleName(), "报表处理类", "/build/table/" + BiClassHandler.class.getSimpleName()
                        , 1, 15, null, eruptMenu), "code", BiClassHandler.class.getSimpleName());
                eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu(BiDimensionReference.class.getSimpleName(), "参照维度", "/build/table/" + BiDimensionReference.class.getSimpleName()
                        , 1, 18, null, eruptMenu), "code", BiDimensionReference.class.getSimpleName());
                eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu(BiFunction.class.getSimpleName(), "函数管理", "/build/table/" + BiFunction.class.getSimpleName()
                        , 1, 20, null, eruptMenu), "code", BiFunction.class.getSimpleName());
                eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu(Bi.class.getSimpleName(), "报表配置", "/build/table/" + Bi.class.getSimpleName()
                        , 1, 30, null, eruptMenu), "code", Bi.class.getSimpleName());
                eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu(BiChart.class.getSimpleName(), "图表配置", "/build/table/" + BiChart.class.getSimpleName()
                        , 2, 100, null, eruptMenu), "code", BiChart.class.getSimpleName());
                eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu(BiDimension.class.getSimpleName(), "维度配置", "/build/table/" + BiDimension.class.getSimpleName()
                        , 2, 120, null, eruptMenu), "code", BiDimension.class.getSimpleName());
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
