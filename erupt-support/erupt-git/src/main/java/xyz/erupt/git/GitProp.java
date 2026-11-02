package xyz.erupt.git;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author YuePeng
 * date 2026/10/21
 */
@Getter
@Setter
@Component
@ConfigurationProperties("erupt.git")
public class GitProp {

    // git executable, resolved from PATH unless absolute
    private String binary = "git";

    // A single git command is killed after this many seconds (clone of a large repository included)
    private int timeoutSeconds = 600;

    // file:// and plain-path remotes read the server's own disk; off unless the deployment needs them
    private boolean allowLocalRemote = false;

    // Committer of the commits erupt writes or rewrites (commit, rebase); authors are set per call
    private String committerName = "erupt";

    private String committerEmail = "erupt@localhost";

}
