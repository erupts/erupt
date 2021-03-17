package xyz.erupt.bi.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.bi.model.*;
import xyz.erupt.core.toolkit.TimeRecorder;
import xyz.erupt.core.util.ProjectUtil;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.enums.MenuTypeEnum;
import xyz.erupt.upms.model.EruptMenu;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author liyuepeng
 * @date 2019-07-15.
 */
@Service
@Order
@Slf4j
public class BiDataInitService implements CommandLineRunner {

    static String defineFunctions;

    @Transactional
    @Override
    public void run(String... args) throws Exception {
        MenuTypeEnum.addMenuType(new VLModel("bi", "报表", "报表编码"));
        new ProjectUtil().projectStartLoaded("bi", first -> {
            if (first) {
                String code = "code";
                String mbi = "$mbi";
                EruptMenu eruptMenu = eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu(mbi, "报表维护",
                        null, null, Integer.valueOf(EruptMenu.OPEN), 20, "fa fa-table", null), code, mbi);
                eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu(BiDataSource.class.getSimpleName(), "数据源管理", MenuTypeEnum.TABLE.getCode(), BiDataSource.class.getSimpleName()
                        , Integer.valueOf(EruptMenu.OPEN), 10, null, eruptMenu), code, BiDataSource.class.getSimpleName());
                eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu(BiClassHandler.class.getSimpleName(), "报表处理类", MenuTypeEnum.TABLE.getCode(), BiClassHandler.class.getSimpleName()
                        , Integer.valueOf(EruptMenu.OPEN), 20, null, eruptMenu), code, BiClassHandler.class.getSimpleName());
                eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu(BiDimensionReference.class.getSimpleName(), "参照维度", MenuTypeEnum.TABLE.getCode(), BiDimensionReference.class.getSimpleName()
                        , Integer.valueOf(EruptMenu.OPEN), 30, null, eruptMenu), code, BiDimensionReference.class.getSimpleName());
                eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu(BiFunction.class.getSimpleName(), "函数管理", MenuTypeEnum.TABLE.getCode(), BiFunction.class.getSimpleName()
                        , Integer.valueOf(EruptMenu.OPEN), 40, null, eruptMenu), code, BiFunction.class.getSimpleName());
                {
                    EruptMenu eruptMenuBi = eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu(Bi.class.getSimpleName(), "报表配置", MenuTypeEnum.TABLE.getCode(), Bi.class.getSimpleName()
                            , Integer.valueOf(EruptMenu.OPEN), 100, null, eruptMenu), code, Bi.class.getSimpleName());
                    eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu(BiChart.class.getSimpleName(), "图表配置", MenuTypeEnum.TABLE.getCode(), BiChart.class.getSimpleName()
                            , Integer.valueOf(EruptMenu.HIDE), 10, null, eruptMenuBi), code, BiChart.class.getSimpleName());
                    eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu(BiHistory.class.getSimpleName(), "修改记录", MenuTypeEnum.TABLE.getCode(), BiHistory.class.getSimpleName()
                            , Integer.valueOf(EruptMenu.HIDE), 20, null, eruptMenuBi), code, BiHistory.class.getSimpleName());
                }
                this.loadDefaultFunction();
            }
        });
        TimeRecorder timeRecorder = new TimeRecorder();
        this.flushFunction();
        log.info("Erupt bi initialization completed in {} ms", timeRecorder.recorder());
    }

    @Resource
    private EruptDao eruptDao;

    @SneakyThrows
    private void loadDefaultFunction() {
        String defaultFunctionCode = "default_function";
        eruptDao.persistIfNotExist(BiFunction.class, new BiFunction(defaultFunctionCode, defaultFunctionCode,
                StreamUtils.copyToString(BiDataInitService.class.getResourceAsStream("./BiDefaultFunction.js")
                        , StandardCharsets.UTF_8)), "code", defaultFunctionCode);
    }

    public void flushFunction() {
        List<Object[]> list = eruptDao.queryObjectList(BiFunction.class, null, null, "jsFunction");
        StringBuilder sb = new StringBuilder();
        for (Object o : list) {
            sb.append((String) o).append("\n");
        }
        defineFunctions = sb.toString();
    }

}
