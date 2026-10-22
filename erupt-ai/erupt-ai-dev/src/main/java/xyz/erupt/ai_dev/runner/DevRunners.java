package xyz.erupt.ai_dev.runner;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import xyz.erupt.ai_dev.prop.EruptAiDevProp;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.core.module.MetaUserinfo;
import xyz.erupt.jpa.dao.EruptDao;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * The coding CLIs erupt knows: the built-in runners and the plain ones added by configuration. The
 * server is scanned for their executables, and the executor picker offers what it found.
 *
 * @author YuePeng
 * date 2026/10/23
 */
@Component
public class DevRunners implements ChoiceFetchHandler<Void> {

    // how long a scan result is trusted: installing a CLI shows up without a restart
    private static final long SCAN_MILLIS = 30_000;

    // codes an agent may still hold from before executors were codes
    private static final Map<String, String> LEGACY = Map.of("claude_code", ClaudeCodeRunner.CODE);

    @Resource
    private List<CodingAgentRunner> builtIns;

    @Resource
    private EruptAiDevProp eruptAiDevProp;

    @Resource
    private EruptDao eruptDao;

    private final Map<String, Long> scannedAt = new ConcurrentHashMap<>();

    private final Map<String, Boolean> found = new ConcurrentHashMap<>();

    public List<CodingAgentRunner> all() {
        Map<String, CodingAgentRunner> all = new LinkedHashMap<>();
        builtIns.forEach(it -> all.put(it.code(), it));
        eruptAiDevProp.getExecutor().getClis().forEach((code, cli) -> {
            if (!all.containsKey(code)) all.put(code, new PlainCliRunner(code, cli));
        });
        return new ArrayList<>(all.values());
    }

    public Optional<CodingAgentRunner> find(String code) {
        if (null == code) return Optional.empty();
        String key = code.toLowerCase(Locale.ROOT);
        String resolved = LEGACY.getOrDefault(key, key);
        return this.all().stream().filter(it -> it.code().equals(resolved)).findFirst();
    }

    public List<CodingAgentRunner> installed() {
        return this.all().stream().filter(this::installed).toList();
    }

    public boolean installed(CodingAgentRunner runner) {
        String binary = runner.binary();
        long now = System.currentTimeMillis();
        Long at = scannedAt.get(binary);
        if (null == at || now - at > SCAN_MILLIS) {
            found.put(binary, null != which(binary));
            scannedAt.put(binary, now);
        }
        return found.get(binary);
    }

    /**
     * The executable a name resolves to: itself when it is a path, else the first hit on the server's PATH.
     */
    public static Path which(String binary) {
        if (StringUtils.isBlank(binary)) return null;
        if (binary.contains("/") || binary.contains(File.separator)) {
            Path path = Path.of(binary);
            return Files.isRegularFile(path) && Files.isExecutable(path) ? path : null;
        }
        String paths = System.getenv("PATH");
        if (null == paths) return null;
        boolean windows = File.separatorChar == '\\';
        for (String dir : paths.split(File.pathSeparator)) {
            if (dir.isBlank()) continue;
            for (String name : windows ? List.of(binary + ".exe", binary + ".cmd", binary) : List.of(binary)) {
                Path path = Path.of(dir, name);
                if (Files.isRegularFile(path) && Files.isExecutable(path)) return path;
            }
        }
        return null;
    }

    /**
     * The executor a new requirement starts with: the one this user picked last in the project, if this
     * server still has it, else the first one installed.
     */
    public String preferred(Long projectId, MetaUserinfo user) {
        List<CodingAgentRunner> installed = this.installed();
        if (installed.isEmpty()) return null;
        if (null != user) {
            List<String> last = eruptDao.getEntityManager().createQuery(
                            "select r.executor from DevRequirement r where r.project.id = :pid and r.creator.id = :uid "
                                    + "and r.executor is not null order by r.id desc", String.class)
                    .setParameter("pid", projectId).setParameter("uid", user.getId()).setMaxResults(1).getResultList();
            if (!last.isEmpty()) {
                Optional<CodingAgentRunner> runner = this.find(last.get(0));
                if (runner.isPresent() && installed.contains(runner.get())) return runner.get().code();
            }
        }
        return installed.get(0).code();
    }

    // a CLI's model list changes rarely and costs a process to read
    private static final long MODELS_MILLIS = 10 * 60_000;

    private static final int MODELS_TIMEOUT_SECONDS = 20;

    private static final int MODELS_MAX = 300;

    private final Map<String, List<String>> models = new ConcurrentHashMap<>();

    private final Map<String, Long> modelsAt = new ConcurrentHashMap<>();

    /**
     * The model ids to offer for an executor: what its CLI lists, else the ones the runner knows; empty
     * when neither, and the field is typed by hand.
     */
    public List<String> models(CodingAgentRunner runner) {
        List<String> command = runner.listModelsCommand();
        if (null == command) return runner.knownModels();
        long now = System.currentTimeMillis();
        Long at = modelsAt.get(runner.code());
        if (null == at || now - at > MODELS_MILLIS) {
            models.put(runner.code(), this.listModels(command, runner.knownModels()));
            modelsAt.put(runner.code(), now);
        }
        return models.get(runner.code());
    }

    private List<String> listModels(List<String> command, List<String> fallback) {
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            DevAgentProcess.clean(builder.environment(), eruptAiDevProp.getExecutor().getEnv());
            Process process = builder.start();
            process.getOutputStream().close();
            List<String> lines = new ArrayList<>();
            try (BufferedReader out = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while (null != (line = out.readLine()) && lines.size() < MODELS_MAX) {
                    String id = line.trim();
                    // ids only: a banner or a log line has spaces in it
                    if (!id.isEmpty() && !id.contains(" ")) lines.add(id);
                }
            }
            if (!process.waitFor(MODELS_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return fallback;
            }
            return process.exitValue() == 0 && !lines.isEmpty() ? lines : fallback;
        } catch (IOException e) {
            return fallback;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return fallback;
        }
    }

    // the executor picker: what this server can run
    @Override
    public List<VLModel> fetch(String[] params) {
        return this.installed().stream().map(it -> new VLModel(it.code(), it.label())).toList();
    }

}
