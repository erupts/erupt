package xyz.erupt.job.service;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import xyz.erupt.auth.model.EruptMenu;
import xyz.erupt.core.util.ProjectUtil;
import xyz.erupt.tool.EruptDao;

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

    @Autowired
    private EruptJobService eruptJobService;

    @Transactional
    @Override
    public void run(String... args) {
        new ProjectUtil().projectStartLoaded("job", first -> {
            if (first) {
                EruptMenu eruptMenu = new EruptMenu("job", "任务管理", null, 1, 10, "fa fa-cubes", null);
                eruptDao.persistIfNotExist(eruptMenu, "code", "job");
                eruptDao.persistIfNotExist(new EruptMenu("EruptJob", "任务维护", "/build/table/EruptJob",
                        1, 0, "fa fa-tasks", eruptMenu), "code", "EruptJob");
                eruptDao.persistIfNotExist(new EruptMenu("EruptJobLog", "任务日志", "/build/table/EruptJobLog",
                        1, 10, "fa fa-file-text", eruptMenu), "code", "EruptJobLog");
            }
        });
    }

}
