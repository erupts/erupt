package xyz.erupt.bi.service;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
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

            }
        });
    }

}
