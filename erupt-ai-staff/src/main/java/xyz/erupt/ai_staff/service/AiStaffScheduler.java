package xyz.erupt.ai_staff.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import xyz.erupt.ai_staff.model.AiStaffTask;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * Holds one cron trigger per enabled staff task; execution itself hops to the
 * async executor so a long-running LLM task never blocks the trigger thread.
 *
 * @author YuePeng
 * date 2026/8/3
 */
@Service
public class AiStaffScheduler implements DisposableBean {

    private final ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();

    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    @Resource
    private AiStaffService aiStaffService;

    @PostConstruct
    public void init() {
        taskScheduler.setThreadNamePrefix("ai-staff-");
        taskScheduler.initialize();
    }

    public synchronized void refresh(AiStaffTask task) {
        this.cancel(task.getId());
        if (!Boolean.TRUE.equals(task.getEnable()) || StringUtils.isBlank(task.getCron())) return;
        // Only the task ID is captured; state is re-read on every run so edits take effect
        scheduledTasks.put(task.getId(), taskScheduler.schedule(() ->
                aiStaffService.executeAsync(task.getId()), new CronTrigger(task.getCron())));
    }

    public synchronized void cancel(Long taskId) {
        Optional.ofNullable(scheduledTasks.remove(taskId)).ifPresent(future -> future.cancel(false));
    }

    @Override
    public void destroy() {
        taskScheduler.shutdown();
    }

}
