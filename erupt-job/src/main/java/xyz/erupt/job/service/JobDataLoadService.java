package xyz.erupt.job.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import xyz.erupt.auth.constant.MenuTypeEnum;
import xyz.erupt.auth.model.EruptMenu;
import xyz.erupt.core.dao.EruptDao;
import xyz.erupt.core.util.ProjectUtil;
import xyz.erupt.job.model.EruptJob;
import xyz.erupt.job.model.EruptJobLog;

import javax.transaction.Transactional;
import java.util.List;

/**
 * @author liyuepeng
 * @date 2019-07-15.
 */
@Service
@Order
@Slf4j
public class JobDataLoadService implements CommandLineRunner {

    @Autowired
    private EruptDao eruptDao;

    @Autowired
    private EruptJobService eruptJobService;

    @Value("${erupt.job.enable:true}")
    private Boolean openJob;

    @Transactional
    @Override
    public void run(String... args) throws Exception {
        if (openJob) {
            List<EruptJob> list = eruptDao.queryEntityList(EruptJob.class, "status = true", null);
            for (EruptJob job : list) {
                eruptJobService.modifyJob(job);
            }
            log.info("Erupt job initialization complete");
        } else {
            log.info("Erupt job disable");
        }
        new ProjectUtil().projectStartLoaded("job", first -> {
            if (first) {
                String job = "$job", code = "code";
                EruptMenu eruptMenu = eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu(job, "任务管理", null, null, 1, 30, "fa fa-cubes", null), code, job);
                eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu(EruptJob.class.getSimpleName(), "任务维护", MenuTypeEnum.TABLE.getCode(), EruptJob.class.getSimpleName(),
                        Integer.valueOf(EruptMenu.OPEN), 0, "fa fa-tasks", eruptMenu), code, EruptJob.class.getSimpleName());
                eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu(EruptJobLog.class.getSimpleName(), "任务日志", MenuTypeEnum.TABLE.getCode(), EruptJobLog.class.getSimpleName(),
                        Integer.valueOf(EruptMenu.OPEN), 10, "fa fa-file-text", eruptMenu), code, EruptJobLog.class.getSimpleName());
            }
        });
    }

}
