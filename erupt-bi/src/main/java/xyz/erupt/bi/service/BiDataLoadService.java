package xyz.erupt.bi.service;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import xyz.erupt.auth.model.EruptMenu;
import xyz.erupt.bi.model.*;
import xyz.erupt.core.dao.EruptDao;
import xyz.erupt.core.util.ProjectUtil;

import javax.transaction.Transactional;
import java.util.ArrayList;
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

    static List<String> functions = new ArrayList<>();

    @Transactional
    @Override
    public void run(String... args) throws Exception {
        new ProjectUtil().projectStartLoaded("bi", first -> {
            if (first) {
                String code = "code";
                String mbi = "$mbi";
                EruptMenu eruptMenu = eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu(mbi, "报表维护",
                        null, Integer.valueOf(EruptMenu.OPEN), 20, "fa fa-table", null), code, mbi);
                eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu(BiDataSource.class.getSimpleName(), "数据源管理", EruptMenu.PATH_TABLE + BiDataSource.class.getSimpleName()
                        , Integer.valueOf(EruptMenu.OPEN), 10, null, eruptMenu), code, BiDataSource.class.getSimpleName());
                eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu(BiClassHandler.class.getSimpleName(), "报表处理类", EruptMenu.PATH_TABLE + BiClassHandler.class.getSimpleName()
                        , Integer.valueOf(EruptMenu.OPEN), 20, null, eruptMenu), code, BiClassHandler.class.getSimpleName());
                eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu(BiDimensionReference.class.getSimpleName(), "参照维度", EruptMenu.PATH_TABLE + BiDimensionReference.class.getSimpleName()
                        , Integer.valueOf(EruptMenu.OPEN), 30, null, eruptMenu), code, BiDimensionReference.class.getSimpleName());
                eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu(BiFunction.class.getSimpleName(), "函数管理", EruptMenu.PATH_TABLE + BiFunction.class.getSimpleName()
                        , Integer.valueOf(EruptMenu.OPEN), 40, null, eruptMenu), code, BiFunction.class.getSimpleName());
                {
                    EruptMenu eruptMenuBi = eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu(Bi.class.getSimpleName(), "报表配置", EruptMenu.PATH_TABLE + Bi.class.getSimpleName()
                            , Integer.valueOf(EruptMenu.OPEN), 100, null, eruptMenu), code, Bi.class.getSimpleName());
                    eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu(BiChart.class.getSimpleName(), "图表配置", EruptMenu.PATH_TABLE + BiChart.class.getSimpleName()
                            , Integer.valueOf(EruptMenu.HIDE), 10, null, eruptMenuBi), code, BiChart.class.getSimpleName());
                    eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu(BiHistory.class.getSimpleName(), "修改记录", EruptMenu.PATH_TABLE + BiHistory.class.getSimpleName()
                            , Integer.valueOf(EruptMenu.HIDE), 20, null, eruptMenuBi), code, BiHistory.class.getSimpleName());
                }
            }
        });
        flushFunction();
        log.info("Erupt bi initialization complete");
    }

    public void flushFunction() {
        List<Object[]> list = eruptDao.queryObjectList(BiFunction.class, null, null, "jsFunction");
        for (Object o : list) {
            functions.add((String) o);
        }
    }

}
