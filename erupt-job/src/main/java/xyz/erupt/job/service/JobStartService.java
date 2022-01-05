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
import xyz.erupt.job.config.EruptJobProp;
import xyz.erupt.job.handler.EruptJobHandler;
import xyz.erupt.job.model.EruptJob;
import xyz.erupt.jpa.dao.EruptDao;

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
public class JobStartService implements CommandLineRunner {

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
    }
}
