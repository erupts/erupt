package xyz.erupt.job.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author YuePeng
 * date 2021/3/28 19:28
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "erupt.job", ignoreUnknownFields = false)
public class EruptJobProp {

    private boolean enable = true;

    // --- Multi-instance dedup lock (Redis-backed, only active when erupt.redis-session=true) ---

    // Max time the cluster lock is held; a crash-safety net so a dead node cannot block the job forever.
    // Must exceed a job's longest expected run time, otherwise the lock may expire mid-run and another
    // instance could execute it concurrently. Default: 30 minutes.
    private long lockAtMostForMillis = 30 * 60 * 1000L;

    // Min time the lock is held after a job finishes; absorbs clock skew between instances so a fast job
    // is not executed twice. Keep it below your shortest cron interval. Default: 1 second.
    private long lockAtLeastForMillis = 1000L;

}
