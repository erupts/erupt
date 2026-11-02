package xyz.erupt.ai_dev.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import xyz.erupt.ai_dev.model.DevProject;
import xyz.erupt.ai_dev.model.DevProjectRepo;
import xyz.erupt.ai_dev.prop.EruptAiDevProp;
import xyz.erupt.core.constant.EruptConst;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

/**
 * Where things live on the server disk:
 * <pre>
 * {root}/repos/{tenant}/{project}/{alias}.git                    bare clone of a project repository
 * {root}/worktrees/{tenant}/{project}/{requirement}/{alias}      a requirement's worktree; the requirement directory is the agent's cwd
 * </pre>
 * Project codes are unique within a tenant only, so the tenant id leads every path. Every segment is
 * checked against its pattern and every path against the root, so nothing a user typed can walk out of it.
 *
 * @author YuePeng
 * date 2026/10/21
 */
@Component
public class DevLayout {

    private static final Pattern TENANT = Pattern.compile("^[A-Za-z0-9_-]{1,64}$");

    private static final Pattern PROJECT = Pattern.compile(DevProject.CODE_REGEX);

    private static final Pattern ALIAS = Pattern.compile(DevProjectRepo.ALIAS_REGEX);

    private static final Pattern REQUIREMENT = Pattern.compile("^req-[0-9]{1,9}$");

    private final Path root;

    public DevLayout(EruptAiDevProp prop) {
        Path root = StringUtils.isBlank(prop.getRoot())
                ? Paths.get(System.getProperty("user.home"), EruptConst.ERUPT_DIR, "ai-dev")
                : Paths.get(prop.getRoot());
        this.root = root.toAbsolutePath().normalize();
    }

    public Path root() {
        return root;
    }

    public Path repoDir(String tenant, String project, String alias) {
        return this.inside(root.resolve("repos").resolve(segment(TENANT, tenant)).resolve(segment(PROJECT, project))
                .resolve(segment(ALIAS, alias) + ".git"));
    }

    public Path worktreeRoot(String tenant, String project) {
        return this.inside(root.resolve("worktrees").resolve(segment(TENANT, tenant)).resolve(segment(PROJECT, project)));
    }

    /**
     * The agent's working directory for one requirement: every repository it touches sits below it.
     */
    public Path workspace(String tenant, String project, String requirementCode) {
        return this.inside(this.worktreeRoot(tenant, project).resolve(segment(REQUIREMENT, requirementDir(requirementCode))));
    }

    public Path worktreeDir(String tenant, String project, String requirementCode, String alias) {
        return this.inside(this.workspace(tenant, project, requirementCode).resolve(segment(ALIAS, alias)));
    }

    // REQ-12 -> req-12
    public static String requirementDir(String requirementCode) {
        return null == requirementCode ? null : requirementCode.toLowerCase();
    }

    private static String segment(Pattern pattern, String value) {
        if (null == value || !pattern.matcher(value).matches()) throw new IllegalArgumentException("Invalid path segment: " + value);
        return value;
    }

    private Path inside(Path path) {
        Path normalized = path.toAbsolutePath().normalize();
        // the segment patterns already rule this out; kept so the layout never widens silently
        if (!normalized.startsWith(root)) throw new IllegalStateException("Path escapes the ai-dev root: " + normalized);
        return normalized;
    }

}
