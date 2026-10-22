package xyz.erupt.git;

import lombok.Getter;

/**
 * A git command that failed, carrying its exit code and stderr.
 *
 * @author YuePeng
 * date 2026/10/21
 */
@Getter
public class GitException extends RuntimeException {

    private final int exitCode;

    private final String stderr;

    public GitException(String message) {
        this(message, -1, "");
    }

    public GitException(String message, int exitCode, String stderr) {
        super(stderr.isBlank() ? message : message + ": " + stderr.strip());
        this.exitCode = exitCode;
        this.stderr = stderr;
    }

}
