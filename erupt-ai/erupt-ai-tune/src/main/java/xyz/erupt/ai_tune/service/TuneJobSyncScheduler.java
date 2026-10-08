package xyz.erupt.ai_tune.service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import xyz.erupt.ai_tune.constants.TuneStatus;
import xyz.erupt.ai_tune.model.TuneJob;
import xyz.erupt.jpa.dao.EruptDao;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Polls the provider for every job still in flight. Training takes minutes to hours and no
 * provider pushes progress, so the console's status, loss curve and event log come from here.
 *
 * @author YuePeng
 * date 2026/10/8
 */
@Component
@Slf4j
public class TuneJobSyncScheduler {

    // An upload that has not produced a remote job id within this window died with the process
    private static final int UPLOAD_GRACE_MINUTES = 60;

    @Resource
    private EruptDao eruptDao;

    @Resource
    private TuneJobService tuneJobService;

    @Scheduled(fixedDelayString = "${erupt.ai.tune.sync-interval:30s}", initialDelayString = "20s")
    public void syncActiveJobs() {
        List<TuneJob> jobs = eruptDao.lambdaQuery(TuneJob.class).in(TuneJob::getStatus, TuneStatus.ACTIVE).list();
        for (TuneJob job : jobs) {
            try {
                if (null == job.getRemoteJobId()) {
                    if (TuneStatus.UPLOADING.equals(job.getStatus()) && null != job.getStartedAt()
                            && job.getStartedAt().plusMinutes(UPLOAD_GRACE_MINUTES).isBefore(LocalDateTime.now())) {
                        tuneJobService.fail(job, "Upload did not complete; the application was probably restarted while the dataset was being sent");
                    }
                    continue;
                }
                tuneJobService.sync(job.getId());
            } catch (Exception e) {
                // A transient provider error must not stop the other jobs from syncing
                log.warn("Fine-tuning job {} sync failed: {}", job.getId(), e.getMessage());
            }
        }
    }

}
