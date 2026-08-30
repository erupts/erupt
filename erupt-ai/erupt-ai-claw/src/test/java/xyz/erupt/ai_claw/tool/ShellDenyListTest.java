package xyz.erupt.ai_claw.tool;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import xyz.erupt.ai_claw.prop.EruptAiClawProp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The shell hardline deny list must block catastrophic commands <em>before</em>
 * any process is started. Every command exercised here matches a HARDLINE_DENY
 * pattern, so execShell returns "Blocked: ..." without ever reaching
 * ProcessBuilder — these tests never spawn a shell.
 *
 * @author YuePeng
 */
public class ShellDenyListTest {

    private EruptSystemTools tools;

    @BeforeEach
    public void setUp() {
        tools = new EruptSystemTools();
        EruptAiClawProp prop = new EruptAiClawProp();
        prop.setEnableExecShell(true);
        ReflectionTestUtils.setField(tools, "eruptAiClawProp", prop);
    }

    // Every command here matches a HARDLINE_DENY pattern and is rejected before ProcessBuilder runs
    private static final String[] CATASTROPHIC = {
            "rm -rf /",
            "rm -rf /*",
            "rm -rf --no-preserve-root /",
            "mkfs.ext4 /dev/sda1",
            ":(){ :|:& };:",
            "shutdown -h now",
            "sudo reboot",
            "dd if=/dev/zero of=/dev/sda",
            "echo boom > /dev/sda"
    };

    @Test
    public void blocksCatastrophicCommands() {
        for (String command : CATASTROPHIC) {
            String result = tools.execShell(command, "", 5);
            assertTrue(result.startsWith("Blocked:"),
                    "expected command to be blocked but got: " + result + " (command: " + command + ")");
        }
    }

    @Test
    public void refusesWhenShellDisabled() {
        EruptAiClawProp disabled = new EruptAiClawProp(); // enableExecShell defaults to false
        ReflectionTestUtils.setField(tools, "eruptAiClawProp", disabled);
        // Disabled check short-circuits before the deny list is even consulted
        assertEquals("Shell execution is disabled", tools.execShell("rm -rf /", "", 5));
    }

    @Test
    public void rejectsEmptyCommand() {
        assertEquals("Error: command is empty", tools.execShell("   ", "", 5));
    }

}
