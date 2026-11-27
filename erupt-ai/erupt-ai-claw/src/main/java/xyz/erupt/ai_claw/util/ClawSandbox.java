package xyz.erupt.ai_claw.util;

import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.context.MetaUser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

/**
 * Per-user sandbox root shared by claw tools: ~/.erupt/{account}, ~/.erupt/.tenants/{tenant}/{account} for a tenant's user
 * (under the user's home directory, so it survives working-directory changes
 * and the framework's .erupt reset on data reload).
 *
 * @author YuePeng
 * date 2026/8/20
 */
public class ClawSandbox {

    // An account name becomes a directory name, and nothing about the account form keeps it
    // from reading as a path: a dot pair or a separator would move every claw tool's sandbox
    // somewhere else on disk. Only these characters survive into the directory name.
    private static final Pattern ILLEGAL_IN_DIR = Pattern.compile("[^A-Za-z0-9_-]");

    // parent of the tenants' sandboxes
    private static final String TENANT_DIR = ".tenants";

    private ClawSandbox() {
    }

    // Sandbox root of the current user, created on first access
    public static Path root() {
        try {
            Path base = Paths.get(System.getProperty("user.home"), EruptConst.ERUPT_DIR).toAbsolutePath().normalize();
            MetaUser user = MetaContext.getUser();
            // an account name is unique within a tenant only: a tenant's users get a directory of the tenant's
            // own, under a name no account can produce (a dot never survives into one), so they never share
            // a sandbox with the platform's account of the same name or with another tenant's
            Path home = null == user.getTenantId() ? base : base.resolve(TENANT_DIR).resolve(dirName(user.getTenantId()));
            Path root = home.resolve(dirName(user.getAccount())).normalize();
            // the substitution above already rules this out; kept so the sandbox never widens silently
            if (!root.startsWith(base)) {
                throw new IllegalStateException("Sandbox escapes its root: " + root);
            }
            Files.createDirectories(root);
            return root;
        } catch (IOException e) {
            throw new RuntimeException("Cannot initialize sandbox: " + e.getMessage(), e);
        }
    }

    /**
     * An account with nothing left to sanitize would resolve the sandbox to the erupt directory
     * itself, which carries every other user's sandbox and the framework's own files, so a blank
     * name is refused rather than widened.
     */
    private static String dirName(String account) {
        String name = null == account ? "" : ILLEGAL_IN_DIR.matcher(account).replaceAll("_");
        if (name.isBlank() || name.chars().allMatch(it -> it == '_')) {
            throw new IllegalStateException("Account does not yield a sandbox directory: " + account);
        }
        return name;
    }

}
