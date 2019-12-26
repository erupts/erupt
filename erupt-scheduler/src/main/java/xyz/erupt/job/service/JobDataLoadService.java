package xyz.erupt.job.service;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import xyz.erupt.core.util.ProjectUtil;
import xyz.erupt.tool.util.EruptDao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

/**
 * Created by liyuepeng on 2019-07-15.
 */
@Service
@Order(10)
@Log
public class JobDataLoadService implements CommandLineRunner {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private EruptDao eruptDao;

    @Transactional
    @Override
    public void run(String... args) {
        new ProjectUtil().projectStartLoaded("job", first -> {
            if (first) {
                System.out.println(123);
            }
        });
    }

}
