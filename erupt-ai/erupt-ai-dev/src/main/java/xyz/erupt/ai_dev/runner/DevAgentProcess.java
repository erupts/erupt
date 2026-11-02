package xyz.erupt.ai_dev.runner;

import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import xyz.erupt.ai_dev.constant.EventKind;
import xyz.erupt.ai_dev.prop.EruptAiDevProp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Runs an agent CLI as a child process and supervises it: clean environment, prompt on stdin, output
 * fed line by line to the runner, killed (with its children) on stop or timeout.
 *
 * @author YuePeng
 * date 2026/10/22
 */
@Slf4j
@Component
public class DevAgentProcess {

    public record Outcome(int exitCode, boolean stopped, boolean timedOut, String stderrTail) {
    }

    // what the agent inherits from the server's own environment; everything else comes from configuration
    static final List<String> INHERITED = List.of("PATH", "HOME", "LANG", "LC_ALL", "TMPDIR", "USER", "SHELL", "SystemRoot", "TEMP", "TMP");

    private static final int STDERR_TAIL = 4000;

    private final Map<Long, Process> running = new ConcurrentHashMap<>();

    private final Set<Long> stopping = ConcurrentHashMap.newKeySet();

    private final ScheduledExecutorService watchdog = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "erupt-ai-dev-watchdog");
        thread.setDaemon(true);
        return thread;
    });

    @Resource
    private EruptAiDevProp eruptAiDevProp;

    @PreDestroy
    void shutdown() {
        running.values().forEach(DevAgentProcess::kill);
        watchdog.shutdownNow();
    }

    /**
     * Runs to completion; {@code key} (the requirement id) is what {@link #stop} addresses.
     */
    public Outcome run(Long key, CodingAgentRunner runner, CodingAgentRunner.Spec spec, CodingAgentRunner.Listener listener) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(runner.command(spec)).directory(spec.workspace().toFile());
        Map<String, String> env = builder.environment();
        clean(env, eruptAiDevProp.getExecutor().getEnv());
        env.putAll(runner.environment(spec));
        Process process = builder.start();
        running.put(key, process);
        AtomicBoolean timedOut = new AtomicBoolean();
        ScheduledFuture<?> timeout = watchdog.schedule(() -> {
            timedOut.set(true);
            kill(process);
        }, eruptAiDevProp.getExecutor().getRunTimeoutMinutes(), TimeUnit.MINUTES);
        StringBuilder stderr = new StringBuilder();
        Thread errReader = new Thread(() -> tail(process.getErrorStream(), stderr), "erupt-ai-dev-stderr");
        errReader.setDaemon(true);
        errReader.start();
        try {
            try (OutputStream in = process.getOutputStream()) {
                if (runner.promptOnStdin()) in.write(spec.prompt().getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                // the CLI may exit before reading its prompt; its exit code tells the rest
                log.debug("Agent stdin closed early: {}", e.getMessage());
            }
            try (BufferedReader out = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while (null != (line = out.readLine())) {
                    try {
                        runner.onLine(line, listener);
                    } catch (RuntimeException e) {
                        // one unreadable line must not end the run
                        listener.event(EventKind.ERROR, "parse", e.getMessage());
                    }
                }
            }
            int exit = process.waitFor();
            errReader.join(TimeUnit.SECONDS.toMillis(5));
            synchronized (stderr) {
                return new Outcome(exit, stopping.contains(key), timedOut.get(), stderr.toString());
            }
        } finally {
            timeout.cancel(false);
            running.remove(key);
            stopping.remove(key);
        }
    }

    /**
     * Kills the run of this key; answers false when nothing runs under it on this instance.
     */
    public boolean stop(Long key) {
        Process process = running.get(key);
        if (null == process) return false;
        stopping.add(key);
        kill(process);
        return true;
    }

    public boolean isRunning(Long key) {
        return running.containsKey(key);
    }

    /**
     * What every process started for an agent sees: PATH, HOME and the like from the server, plus the
     * configured variables, so no secret of the server reaches it.
     */
    static void clean(Map<String, String> env, Map<String, String> configured) {
        Map<String, String> inherited = new java.util.HashMap<>();
        for (String name : INHERITED) {
            String value = System.getenv(name);
            if (null != value) inherited.put(name, value);
        }
        env.clear();
        env.putAll(inherited);
        env.putAll(configured);
    }

    private static void kill(Process process) {
        process.descendants().forEach(ProcessHandle::destroyForcibly);
        process.destroyForcibly();
    }

    private static void tail(InputStream in, StringBuilder sink) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while (null != (line = reader.readLine())) {
                synchronized (sink) {
                    sink.append(line).append('\n');
                    if (sink.length() > STDERR_TAIL) sink.delete(0, sink.length() - STDERR_TAIL);
                }
            }
        } catch (IOException ignored) {
            // the process ended
        }
    }

}
