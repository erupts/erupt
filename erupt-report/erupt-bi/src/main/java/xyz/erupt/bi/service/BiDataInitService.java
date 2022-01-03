package xyz.erupt.bi.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.bi.model.*;
import xyz.erupt.core.constant.MenuTypeEnum;
import xyz.erupt.core.toolkit.TimeRecorder;
import xyz.erupt.core.util.ProjectUtil;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.enums.MenuStatus;
import xyz.erupt.upms.model.EruptMenu;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author YuePeng
 * date 2019-07-15.
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
                String mbi = "$mbi";
                EruptMenu eruptMenu = eruptDao.persistIfNotExist(EruptMenu.class, EruptMenu.createSimpleMenu(mbi, "报表维护", "fa fa-table", 20), EruptMenu.CODE, mbi);
                eruptDao.persistIfNotExist(EruptMenu.class, EruptMenu.createEruptClassMenu(BiDataSource.class, eruptMenu, 10), EruptMenu.CODE, BiDataSource.class.getSimpleName());
                eruptDao.persistIfNotExist(EruptMenu.class, EruptMenu.createEruptClassMenu(BiClassHandler.class, eruptMenu, 20), EruptMenu.CODE, BiClassHandler.class.getSimpleName());
                eruptDao.persistIfNotExist(EruptMenu.class, EruptMenu.createEruptClassMenu(BiTpl.class, eruptMenu, 25), EruptMenu.CODE, BiTpl.class.getSimpleName());
                eruptDao.persistIfNotExist(EruptMenu.class, EruptMenu.createEruptClassMenu(BiDimensionReference.class, eruptMenu, 30), EruptMenu.CODE, BiDimensionReference.class.getSimpleName());
                eruptDao.persistIfNotExist(EruptMenu.class, EruptMenu.createEruptClassMenu(BiFunction.class, eruptMenu, 40), EruptMenu.CODE, BiFunction.class.getSimpleName());
                eruptDao.persistIfNotExist(EruptMenu.class, EruptMenu.createEruptClassMenu(BiGroup.class, eruptMenu, 50), EruptMenu.CODE, BiGroup.class.getSimpleName());
                {
                    EruptMenu eruptMenuBi = eruptDao.persistIfNotExist(EruptMenu.class, EruptMenu.createEruptClassMenu(Bi.class, eruptMenu, 100), EruptMenu.CODE, Bi.class.getSimpleName());
                    eruptDao.persistIfNotExist(EruptMenu.class, EruptMenu.createEruptClassMenu(BiChart.class, eruptMenuBi, 10, MenuStatus.HIDE), EruptMenu.CODE, BiChart.class.getSimpleName());
                    eruptDao.persistIfNotExist(EruptMenu.class, EruptMenu.createEruptClassMenu(BiHistory.class, eruptMenuBi, 20, MenuStatus.HIDE), EruptMenu.CODE, BiHistory.class.getSimpleName());
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
        String defaultFunctionCode = "simple_function";
        eruptDao.persistIfNotExist(BiFunction.class, new BiFunction(defaultFunctionCode,
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
