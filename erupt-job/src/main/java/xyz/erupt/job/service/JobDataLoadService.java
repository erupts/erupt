package xyz.erupt.job.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.core.annotation.EruptHandlerNaming;
import xyz.erupt.core.service.EruptApplication;
import xyz.erupt.core.toolkit.TimeRecorder;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.core.util.ProjectUtil;
import xyz.erupt.job.config.EruptJobProp;
import xyz.erupt.job.handler.EruptJobHandler;
import xyz.erupt.job.model.EruptJob;
import xyz.erupt.job.model.EruptJobLog;
import xyz.erupt.job.model.EruptMail;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.model.EruptMenu;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

/**
 * @author YuePeng
 * date 2019-07-15.
 */
@Service
@Order
@Slf4j
public class JobDataLoadService implements CommandLineRunner {

    @Resource
    private EruptDao eruptDao;

    @Resource
    private EruptJobService eruptJobService;

    @Resource
    private EruptJobProp eruptJobProp;

    private static final List<VLModel> loadedJobHandler = new ArrayList<>();

    public static List<VLModel> getLoadedJobHandler() {
        return loadedJobHandler;
    }

    @Transactional
    @Override
    public void run(String... args) throws Exception {
        TimeRecorder timeRecorder = new TimeRecorder();
        EruptSpringUtil.scannerPackage(EruptApplication.getScanPackage(), new TypeFilter[]{new AssignableTypeFilter(EruptJobHandler.class)}, clazz -> {
            EruptHandlerNaming eruptHandlerNaming = clazz.getAnnotation(EruptHandlerNaming.class);
            loadedJobHandler.add(new VLModel(clazz.getName(), (null == eruptHandlerNaming) ? clazz.getSimpleName() : eruptHandlerNaming.value()));
        });
        if (eruptJobProp.isEnable()) {
            for (EruptJob job : eruptDao.queryEntityList(EruptJob.class, "status = true", null)) {
                eruptJobService.modifyJob(job);
            }
            log.info("Erupt job initialization completed in {} ms", timeRecorder.recorder());
        } else {
            log.info("Erupt job disable");
        }
        new ProjectUtil().projectStartLoaded("job", first -> {
            if (first) {
                String job = "$job";
                EruptMenu eruptMenu = eruptDao.persistIfNotExist(EruptMenu.class, EruptMenu.createSimpleMenu(job, "任务管理", "fa fa-cubes", 30), EruptMenu.CODE, job);
                eruptDao.persistIfNotExist(EruptMenu.class, EruptMenu.createEruptClassMenu(EruptJob.class, eruptMenu, 0), EruptMenu.CODE, EruptJob.class.getSimpleName());
                eruptDao.persistIfNotExist(EruptMenu.class, EruptMenu.createEruptClassMenu(EruptJobLog.class, eruptMenu, 10), EruptMenu.CODE, EruptJobLog.class.getSimpleName());
                eruptDao.persistIfNotExist(EruptMenu.class, EruptMenu.createEruptClassMenu(EruptMail.class, eruptMenu, 20), EruptMenu.CODE, EruptMail.class.getSimpleName());
            }
        });
    }
}
