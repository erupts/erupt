package xyz.erupt.monitor.vo;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.monitor.util.SystemUtil;

import java.lang.management.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Deep JVM diagnosis sourced purely from JDK MXBeans: GC activity, memory pools
 * (Eden / Old / Metaspace ...), class loading, thread states and deadlocks.
 *
 * @author YuePeng
 * date 2026/6/15
 */
@Getter
@Setter
public class JvmDiagnosis {

    private List<Gc> gc;

    private List<MemoryPool> memoryPools;

    private ClassLoad classLoading;

    private ThreadStat threads;

    private Compilation compilation;

    public JvmDiagnosis() {
        this.gc = ManagementFactory.getGarbageCollectorMXBeans().stream().map(Gc::new).collect(Collectors.toList());
        this.memoryPools = ManagementFactory.getMemoryPoolMXBeans().stream().map(MemoryPool::new).collect(Collectors.toList());
        this.classLoading = new ClassLoad();
        this.threads = new ThreadStat();
        this.compilation = new Compilation();
    }

    @Getter
    @Setter
    public static class Gc {
        private String name;
        private long count;
        private long time; // total gc time, ms

        Gc(GarbageCollectorMXBean bean) {
            this.name = bean.getName();
            this.count = bean.getCollectionCount();
            this.time = bean.getCollectionTime();
        }
    }

    @Getter
    @Setter
    public static class MemoryPool {
        private String name;
        private String type; // HEAP / NON_HEAP
        private long used;
        private long committed;
        private long max;
        private String usedText;
        private String committedText;
        private String maxText;
        private String usage; // percent against max (or committed when max is undefined)

        MemoryPool(MemoryPoolMXBean bean) {
            MemoryUsage usage = bean.getUsage();
            this.name = bean.getName();
            this.type = bean.getType().name();
            this.used = usage.getUsed();
            this.committed = usage.getCommitted();
            this.max = usage.getMax();
            this.usedText = SystemUtil.formatByte(used);
            this.committedText = SystemUtil.formatByte(committed);
            this.maxText = max < 0 ? "-" : SystemUtil.formatByte(max);
            long base = max > 0 ? max : committed;
            this.usage = base <= 0 ? "0%" : new DecimalFormat("#.##%").format(used * 1.0 / base);
        }
    }

    @Getter
    @Setter
    public static class ClassLoad {
        private long loaded;
        private long total;
        private long unloaded;

        ClassLoad() {
            ClassLoadingMXBean bean = ManagementFactory.getClassLoadingMXBean();
            this.loaded = bean.getLoadedClassCount();
            this.total = bean.getTotalLoadedClassCount();
            this.unloaded = bean.getUnloadedClassCount();
        }
    }

    @Getter
    @Setter
    public static class ThreadStat {
        private int live;
        private int daemon;
        private int peak;
        private long totalStarted;
        private int deadlock;
        private Map<String, Integer> states; // Thread.State -> count

        ThreadStat() {
            ThreadMXBean bean = ManagementFactory.getThreadMXBean();
            this.live = bean.getThreadCount();
            this.daemon = bean.getDaemonThreadCount();
            this.peak = bean.getPeakThreadCount();
            this.totalStarted = bean.getTotalStartedThreadCount();
            long[] deadlocked = bean.findDeadlockedThreads();
            this.deadlock = deadlocked == null ? 0 : deadlocked.length;
            Map<String, Integer> states = new LinkedHashMap<>();
            for (Thread.State state : Thread.State.values()) {
                states.put(state.name(), 0);
            }
            ThreadInfo[] infos = bean.getThreadInfo(bean.getAllThreadIds());
            for (ThreadInfo info : infos) {
                if (info != null) {
                    states.merge(info.getThreadState().name(), 1, Integer::sum);
                }
            }
            this.states = states;
        }
    }

    @Getter
    @Setter
    public static class Compilation {
        private String name;
        private long totalTimeMs;
        private boolean supported;

        Compilation() {
            CompilationMXBean bean = ManagementFactory.getCompilationMXBean();
            if (bean != null) {
                this.name = bean.getName();
                this.supported = bean.isCompilationTimeMonitoringSupported();
                this.totalTimeMs = this.supported ? bean.getTotalCompilationTime() : 0;
            }
        }
    }

    /**
     * Full thread dump text, used by the on-demand download endpoint.
     */
    public static String dumpThreads() {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        StringBuilder sb = new StringBuilder();
        long[] deadlocked = bean.findDeadlockedThreads();
        if (deadlocked != null && deadlocked.length > 0) {
            sb.append("===== DEADLOCK DETECTED: ").append(deadlocked.length).append(" thread(s) =====\n\n");
        }
        List<String> lines = new ArrayList<>();
        for (ThreadInfo info : bean.dumpAllThreads(true, true)) {
            lines.add(info.toString());
        }
        sb.append(String.join("", lines));
        return sb.toString();
    }

}
