package xyz.erupt.git;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Runs the git executable. Arguments go to the process as an array, never through a shell, and every
 * value a caller supplies (ref, URL, path) is checked here first so it cannot be read as an option.
 * Credentials travel in the child's environment through an inline credential helper, so no secret
 * ever appears on a command line, in a URL or on disk.
 *
 * @author YuePeng
 * date 2026/10/21
 */
public class GitCli {

    public record Result(int exitCode, String stdout, String stderr) {

        public boolean ok() {
            return exitCode == 0;
        }

        public List<String> lines() {
            return stdout.isEmpty() ? List.of() : List.of(stdout.split("\n"));
        }
    }

    private static final String ENV_USER = "ERUPT_GIT_USERNAME";

    private static final String ENV_PASSWORD = "ERUPT_GIT_PASSWORD";

    // Answers git's credential request from the environment; the shell expands the variables at run time,
    // so only their names are on the command line
    private static final String CREDENTIAL_HELPER = "!f() { echo \"username=${" + ENV_USER + "}\"; echo \"password=${" + ENV_PASSWORD + "}\"; }; f";

    private static final Pattern REF = Pattern.compile("[A-Za-z0-9_][A-Za-z0-9._/-]*");

    private static final Pattern SCP_URL = Pattern.compile("[A-Za-z0-9._-]+@[A-Za-z0-9.-]+:[A-Za-z0-9._~/-]+");

    private static final Pattern REMOTE_URL = Pattern.compile("(?i)(https?|ssh)://[^\\s]+");

    private static final Pattern LOCAL_URL = Pattern.compile("(?i)file://[^\\s]+");

    // https://user:token@host would be written to the repository config in clear: pass a GitAuth instead
    private static final Pattern URL_USERINFO = Pattern.compile("(?i)https?://[^/\\s]*@.*");

    // Draining a child's output blocks, so it gets its own threads rather than the common pool
    private static final ExecutorService READERS = Executors.newCachedThreadPool(r -> {
        Thread thread = new Thread(r, "erupt-git-io");
        thread.setDaemon(true);
        return thread;
    });

    private final GitProp prop;

    public GitCli(GitProp prop) {
        this.prop = prop;
    }

    // ------------------------------------------------------------------ running

    /**
     * Runs git and answers its stdout, failing on a non-zero exit.
     */
    public String run(Path dir, String... args) {
        return this.run(dir, GitAuth.NONE, Map.of(), args);
    }

    public String run(Path dir, GitAuth auth, Map<String, String> env, String... args) {
        Result result = this.exec(dir, auth, env, args);
        if (!result.ok()) throw new GitException("git " + args[0] + " failed", result.exitCode(), result.stderr());
        return result.stdout();
    }

    /**
     * Runs git and answers whatever it produced, leaving the exit code to the caller.
     */
    public Result exec(Path dir, GitAuth auth, Map<String, String> env, String... args) {
        List<String> command = new ArrayList<>();
        command.add(prop.getBinary());
        if (null != dir) {
            command.add("-C");
            command.add(dir.toString());
        }
        // repository hooks are code from the repository: never run them on the server
        command.addAll(List.of("-c", "core.hooksPath=" + Path.of(System.getProperty("java.io.tmpdir"), "erupt-git-no-hooks")));
        command.addAll(List.of("-c", "protocol.ext.allow=never"));
        if (auth.hasPassword()) command.addAll(List.of("-c", "credential.helper=", "-c", "credential.helper=" + CREDENTIAL_HELPER));
        command.addAll(Arrays.asList(args));
        ProcessBuilder builder = new ProcessBuilder(command);
        Map<String, String> environment = builder.environment();
        environment.put("GIT_TERMINAL_PROMPT", "0");
        environment.put("LC_ALL", "C");
        environment.put("GIT_SSH_COMMAND", "ssh -o BatchMode=yes -o StrictHostKeyChecking=accept-new"
                + (null == auth.sshKey() ? "" : " -o IdentitiesOnly=yes -i '" + auth.sshKey() + "'"));
        if (auth.hasPassword()) {
            environment.put(ENV_USER, null == auth.username() ? "git" : auth.username());
            environment.put(ENV_PASSWORD, auth.password());
        }
        environment.putAll(env);
        try {
            Process process = builder.start();
            process.getOutputStream().close();
            CompletableFuture<String> stdout = CompletableFuture.supplyAsync(() -> read(process.getInputStream()), READERS);
            CompletableFuture<String> stderr = CompletableFuture.supplyAsync(() -> read(process.getErrorStream()), READERS);
            if (!process.waitFor(prop.getTimeoutSeconds(), TimeUnit.SECONDS)) {
                process.destroyForcibly();
                throw new GitException("git " + args[0] + " timed out after " + prop.getTimeoutSeconds() + "s");
            }
            return new Result(process.exitValue(), stdout.join().stripTrailing(), stderr.join());
        } catch (IOException e) {
            throw new GitException("Cannot run " + prop.getBinary() + ": " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GitException("Interrupted while running git " + args[0]);
        }
    }

    private static String read(InputStream in) {
        try (in) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // ------------------------------------------------------------------ argument checks

    /**
     * A branch or ref name: no leading dash (an option), no "..", "//", "@{" or ".lock" that git refuses
     * or reads as a range.
     */
    public static String ref(String name) {
        if (null == name || !REF.matcher(name).matches() || name.contains("..") || name.contains("//")
                || name.endsWith("/") || name.endsWith(".") || name.endsWith(".lock")) {
            throw new GitException("Invalid git ref: " + name);
        }
        return name;
    }

    /**
     * A remote URL: https, ssh or scp-like; file:// only when the deployment allows local remotes.
     * Transport helpers ("ext::", "fd::") and anything option-like are refused.
     */
    public String url(String url) {
        if (null != url && URL_USERINFO.matcher(url).matches()) {
            throw new GitException("Credentials in the remote URL are not allowed, use GitAuth");
        }
        if (null != url && (REMOTE_URL.matcher(url).matches() || SCP_URL.matcher(url).matches()
                || (prop.isAllowLocalRemote() && LOCAL_URL.matcher(url).matches()))) {
            return url;
        }
        throw new GitException("Unsupported git remote URL");
    }

    /**
     * An absolute, normalized file system path.
     */
    public static Path path(Path path) {
        if (null == path || !path.isAbsolute()) throw new GitException("Path must be absolute: " + path);
        return path.normalize();
    }

}
