package xyz.erupt.git.model;

import xyz.erupt.git.GitException;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * A commit author or committer.
 *
 * @author YuePeng
 * date 2026/10/21
 */
public record GitIdent(String name, String email) {

    // git rejects angle brackets and line breaks in an ident, and they would corrupt the commit header
    private static final Pattern UNSAFE = Pattern.compile("[<>\\r\\n]");

    public GitIdent {
        if (null == name || name.isBlank() || UNSAFE.matcher(name).find()
                || null == email || email.isBlank() || UNSAFE.matcher(email).find()) {
            throw new GitException("Invalid git identity");
        }
    }

    public Map<String, String> authorEnv() {
        return Map.of("GIT_AUTHOR_NAME", name, "GIT_AUTHOR_EMAIL", email);
    }

    public Map<String, String> committerEnv() {
        return Map.of("GIT_COMMITTER_NAME", name, "GIT_COMMITTER_EMAIL", email);
    }

}
