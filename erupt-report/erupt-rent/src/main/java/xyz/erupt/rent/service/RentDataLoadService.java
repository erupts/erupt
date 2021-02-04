package xyz.erupt.rent.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import xyz.erupt.jpa.dao.EruptDao;

import javax.transaction.Transactional;

/**
 * @author liyuepeng
 * @date 2019-07-15.
 */
@Service
@Order(10)
@Slf4j
public class RentDataLoadService implements CommandLineRunner {

    @Autowired
    private EruptDao eruptDao;

    @Transactional
    @Override
    public void run(String... args) throws Exception {
//        new ProjectUtil().projectStartLoaded("rent", first -> {
//            if (first) {
//
//            }
//        });

    }

}
