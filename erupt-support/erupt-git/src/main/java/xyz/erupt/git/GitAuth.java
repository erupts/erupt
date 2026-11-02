package xyz.erupt.git;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

/**
 * Credentials for one remote call. They reach git through the child process environment only: never the
 * command line, the remote URL or {@code .git/config}, so nothing is left on disk or in a process listing.
 * The caller resolves them from its secret store; erupt-git keeps none.
 *
 * @param username HTTPS user name, for a token most hosts accept any non-empty value
 * @param password HTTPS password or access token
 * @param sshKey   private key file for SSH remotes
 * @author YuePeng
 * date 2026/10/21
 */
public record GitAuth(String username, String password, Path sshKey) {

    public static final GitAuth NONE = new GitAuth(null, null, null);

    // GIT_SSH_COMMAND is read by a shell, so the key path may not carry anything a shell would interpret
    private static final Pattern SAFE_PATH = Pattern.compile("[A-Za-z0-9._/\\\\:-]+");

    public static GitAuth token(String token) {
        return new GitAuth("x-access-token", token, null);
    }

    public static GitAuth basic(String username, String password) {
        return new GitAuth(username, password, null);
    }

    public static GitAuth ssh(Path key) {
        String path = key.toAbsolutePath().normalize().toString();
        if (!SAFE_PATH.matcher(path).matches()) throw new GitException("Unsupported characters in SSH key path");
        if (!Files.isRegularFile(key)) throw new GitException("SSH key not found");
        return new GitAuth(null, null, key.toAbsolutePath().normalize());
    }

    public GitAuth {
        // the credential protocol is line based: a line break would smuggle extra attributes into it
        if ((null != username && username.matches("(?s).*[\\r\\n].*")) || (null != password && password.matches("(?s).*[\\r\\n].*"))) {
            throw new GitException("Line breaks are not allowed in git credentials");
        }
    }

    boolean hasPassword() {
        return null != password;
    }

    // Never print the secret, records would by default
    @Override
    public String toString() {
        return "GitAuth[" + (null != sshKey ? "ssh" : hasPassword() ? "password" : "none") + "]";
    }

}
