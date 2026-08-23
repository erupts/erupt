package xyz.erupt.ai_claw.util;

import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.context.MetaContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Per-user sandbox root shared by claw tools: ~/.erupt/{account}
 * (under the user's home directory, so it survives working-directory changes
 * and the framework's .erupt reset on data reload).
 *
 * @author YuePeng
 * date 2026/8/20
 */
public class ClawSandbox {

    private ClawSandbox() {
    }

    // Sandbox root of the current user, created on first access
    public static Path root() {
        try {
            Path root = Paths.get(System.getProperty("user.home"), EruptConst.ERUPT_DIR, MetaContext.getUser().getAccount())
                    .toAbsolutePath().normalize();
            Files.createDirectories(root);
            return root;
        } catch (IOException e) {
            throw new RuntimeException("Cannot initialize sandbox: " + e.getMessage(), e);
        }
    }

}
