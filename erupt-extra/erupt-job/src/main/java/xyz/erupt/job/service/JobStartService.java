package xyz.erupt.job.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import xyz.erupt.job.config.EruptJobProp;
import xyz.erupt.job.model.EruptJob;
import xyz.erupt.jpa.dao.EruptDao;

import javax.annotation.Resource;

/**
 * @author YuePeng
 * date 2019-07-15.
 */
@Service
@Order
@Slf4j
public class JobStartService {

    @Resource
    private EruptDao eruptDao;

    @Resource
    private EruptJobService eruptJobService;

    @Resource
    private EruptJobProp eruptJobProp;

    @SneakyThrows
    public void run() {
        if (eruptJobProp.isEnable()) {
            for (EruptJob job : eruptDao.queryEntityList(EruptJob.class, "status = true", null)) {
                eruptJobService.modifyJob(job);
            }
        } else {
            log.info("Erupt job disable");
        }
    }

}
