package xyz.erupt.job.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.stereotype.Service;
import xyz.erupt.core.service.EruptApplication;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.core.util.ProjectUtil;
import xyz.erupt.job.handler.EruptJobHandler;
import xyz.erupt.job.model.EruptJob;
import xyz.erupt.job.model.EruptJobLog;
import xyz.erupt.job.model.EruptMail;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.constant.MenuTypeEnum;
import xyz.erupt.upms.model.EruptMenu;

import javax.transaction.Transactional;
import java.util.ArrayList;
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

    private static final List<String> loadedJobHandler = new ArrayList<>();

    public static List<String> getLoadedJobHandler() {
        return loadedJobHandler;
    }

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
                eruptDao.persistIfNotExist(EruptMail.class, new EruptMenu(EruptMail.class.getSimpleName(), "发送邮件", MenuTypeEnum.TABLE.getCode(), EruptMail.class.getSimpleName(),
                        Integer.valueOf(EruptMenu.OPEN), 20, "fa fa-envelope-o", eruptMenu), code, EruptMail.class.getSimpleName());
            }
        });
        EruptSpringUtil.scannerPackage(EruptApplication.getScanPackage(), new TypeFilter[]{
                new AssignableTypeFilter(EruptJobHandler.class)
        }, clazz -> loadedJobHandler.add(clazz.getName()));
    }
}
