package xyz.erupt.generator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import xyz.erupt.core.util.ProjectUtil;
import xyz.erupt.generator.model.GeneratorClass;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.model.EruptMenu;

import javax.annotation.Resource;
import javax.transaction.Transactional;

/**
 * @author YuePeng
 * date 2019-07-15.
 */
@Service
@Order
@Slf4j
public class GeneratorDataLoadService implements CommandLineRunner {

    @Resource
    private EruptDao eruptDao;

    @Transactional
    @Override
    public void run(String... args) {
        new ProjectUtil().projectStartLoaded("generator", first -> {
            if (first) {
                String generator = "$generator";
                EruptMenu eruptMenu = eruptDao.persistIfNotExist(EruptMenu.class, EruptMenu.createSimpleMenu(generator, "代码生成", "fa fa-code", 40), EruptMenu.CODE, generator);
                eruptDao.persistIfNotExist(EruptMenu.class, EruptMenu.createEruptClassMenu(GeneratorClass.class, eruptMenu, 0), EruptMenu.CODE, GeneratorClass.class.getSimpleName());
            }
        });
    }

}
