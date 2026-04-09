package xyz.erupt.ai_claw.tool;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.FixedDelayTask;
import org.springframework.scheduling.config.FixedRateTask;
import org.springframework.scheduling.config.ScheduledTaskHolder;
import org.springframework.stereotype.Component;
import xyz.erupt.ai.annotation.AiToolbox;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

@AiToolbox
@Component
@ConditionalOnProperty(name = "erupt.ai.claw.enabled", havingValue = "true")
public class EruptRunTimeTools {

    @Resource
    private Environment environment;

    @Resource
    private ApplicationContext applicationContext;

    @Tool("Get currently active Spring profiles (e.g. dev, prod, staging). " +
            "Helps understand the current runtime environment before taking any action.")
    public String getActiveProfiles() {
        String[] profiles = environment.getActiveProfiles();
        return profiles.length == 0 ? "No active profiles (using default)" : String.join(", ", profiles);
    }

    @Tool("Get a Spring application configuration property value by key (e.g. spring.datasource.url). " +
            "Reads from application.yml/properties and all active Spring Environment sources. " +
            "Returns the resolved runtime value, not the raw file content.")
    public String getProperty(
            @P("Spring property key, e.g. spring.datasource.url or erupt.ai.model") String key) {
        String value = environment.getProperty(key);
        return value != null ? key + " = " + value : "Property not found: " + key;
    }

    @Tool("Get JVM heap memory usage of the current process: used, total, and max heap in MB.")
    public String getJvmMemory() {
        Runtime rt = Runtime.getRuntime();
        long used = (rt.totalMemory() - rt.freeMemory()) / 1024 / 1024;
        long total = rt.totalMemory() / 1024 / 1024;
        long max = rt.maxMemory() / 1024 / 1024;
        return "Heap used: " + used + "MB / total: " + total + "MB / max: " + max + "MB";
    }

    @Tool("Get JVM thread statistics: total thread count and deadlocked thread IDs if any. " +
            "Useful for diagnosing hangs or thread exhaustion.")
    public String getThreadInfo() {
        var bean = ManagementFactory.getThreadMXBean();
        long[] deadlocked = bean.findDeadlockedThreads();
        String deadlockInfo = deadlocked == null ? "none" : deadlocked.length + " deadlocked thread(s) detected!";
        return "Thread count: " + bean.getThreadCount()
                + "\nPeak thread count: " + bean.getPeakThreadCount()
                + "\nDeadlocks: " + deadlockInfo;
    }

    @Tool("Get JVM process uptime and start time. Useful for understanding how long the application has been running.")
    public String getJvmUptime() {
        var bean = ManagementFactory.getRuntimeMXBean();
        long uptimeMs = bean.getUptime();
        long hours = TimeUnit.MILLISECONDS.toHours(uptimeMs);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(uptimeMs) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(uptimeMs) % 60;
        String startTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault())
                .format(Instant.ofEpochMilli(bean.getStartTime()));
        return "Start time: " + startTime + "\nUptime: " + hours + "h " + minutes + "m " + seconds + "s";
    }

    @Tool("Get JVM garbage collection statistics: GC name, collection count and total time spent. " +
            "Useful for diagnosing GC pressure or memory issues.")
    public String getGcStats() {
        StringJoiner sj = new StringJoiner("\n");
        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            sj.add(gc.getName() + ": count=" + gc.getCollectionCount() + ", time=" + gc.getCollectionTime() + "ms");
        }
        return sj.toString();
    }

    @Tool("List all Spring @Scheduled tasks: their type (cron/fixedRate/fixedDelay) and schedule expression. " +
            "Useful for understanding what background jobs are running and when.")
    public String getScheduledTasks() {
        StringJoiner sj = new StringJoiner("\n");
        applicationContext.getBeansOfType(ScheduledTaskHolder.class).values().forEach(holder ->
                holder.getScheduledTasks().forEach(task -> {
                    var t = task.getTask();
                    if (t instanceof CronTask ct) {
                        sj.add("cron[" + ct.getExpression() + "] -> " + ct.getRunnable());
                    } else if (t instanceof FixedRateTask fr) {
                        sj.add("fixedRate[" + fr.getIntervalDuration().toMillis() + "ms] -> " + fr.getRunnable());
                    } else if (t instanceof FixedDelayTask fd) {
                        sj.add("fixedDelay[" + fd.getIntervalDuration().toMillis() + "ms] -> " + fd.getRunnable());
                    } else {
                        sj.add("unknown -> " + t.getRunnable());
                    }
                })
        );
        return sj.toString().isEmpty() ? "No scheduled tasks found" : sj.toString();
    }

}
