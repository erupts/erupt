package xyz.erupt.bi.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.bi.model.BiFunction;
import xyz.erupt.core.constant.MenuTypeEnum;
import xyz.erupt.core.toolkit.TimeRecorder;
import xyz.erupt.core.util.ProjectUtil;
import xyz.erupt.jpa.dao.EruptDao;

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
