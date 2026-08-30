package xyz.erupt.ai_claw.tool;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.context.MetaUser;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * File tools must confine every operation to the per-user sandbox
 * (~/.erupt/{account}). user.home is redirected to a {@link TempDir} and a
 * MetaContext user is registered so the sandbox resolves inside the temp
 * directory, letting these tests exercise real filesystem behavior hermetically.
 *
 * @author YuePeng
 */
public class FileSandboxTest {

    private static final String ACCOUNT = "tester";

    private String originalHome;
    private Path sandboxRoot;
    private EruptFileTools tools;

    @BeforeEach
    public void setUp(@TempDir Path home) {
        originalHome = System.getProperty("user.home");
        System.setProperty("user.home", home.toString());
        MetaContext.register(new MetaUser(1L, ACCOUNT, "Tester"));
        sandboxRoot = home.resolve(".erupt").resolve(ACCOUNT);
        tools = new EruptFileTools();
    }

    @AfterEach
    public void tearDown() {
        MetaContext.remove();
        if (originalHome != null) System.setProperty("user.home", originalHome);
    }

    @Test
    public void writeThenReadRoundTrips() {
        assertTrue(tools.writeFile("notes/a.txt", "hello").startsWith("File written"));
        assertEquals("hello", tools.readFile("notes/a.txt"));
        assertTrue(tools.listFiles("").contains("notes"));
    }

    @Test
    public void rejectsParentTraversalOnRead() {
        String result = tools.readFile("../../../../etc/passwd");
        assertTrue(result.contains("escape"), "traversal should be rejected, got: " + result);
    }

    @Test
    public void rejectsParentTraversalOnWriteAndCreatesNothingOutside() throws Exception {
        String result = tools.writeFile("../escapee.txt", "x");
        assertTrue(result.startsWith("Error"), "traversal write should error, got: " + result);
        // Nothing may be written above the sandbox root
        assertFalse(Files.exists(sandboxRoot.getParent().resolve("escapee.txt")));
    }

    @Test
    public void cannotDeleteSandboxRoot() {
        assertEquals("Cannot delete the sandbox root", tools.deleteFile(""));
    }

    @Test
    public void readMissingFileReportsNotFound() {
        assertTrue(tools.readFile("nope.txt").startsWith("File not found"));
    }

    @Test
    public void rejectsReadingOversizeFile() throws Exception {
        Files.createDirectories(sandboxRoot);
        Files.write(sandboxRoot.resolve("big.bin"), new byte[512 * 1024 + 1]);
        assertTrue(tools.readFile("big.bin").contains("too large"));
    }

}
