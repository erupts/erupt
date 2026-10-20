package xyz.erupt.test.filecenter;

import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import xyz.erupt.filecenter.store.FileEntry;
import xyz.erupt.filecenter.store.SftpFileStore;
import xyz.erupt.filecenter.store.SftpSettings;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The SFTP store against an in-process Apache MINA SSHD server serving a temp directory; no Spring
 * context is needed, the store is built by hand. The credentials are the fixture's own.
 */
class SftpFileStoreTest {

    @TempDir
    static Path home;

    private static SshServer server;

    private static SftpFileStore store;

    @BeforeAll
    static void startServer() throws IOException {
        server = SshServer.setUpDefaultServer();
        server.setPort(0);
        server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(home.resolve("hostkey.ser")));
        server.setPasswordAuthenticator((user, password, session) -> "demo".equals(user) && "demo-pass".equals(password));
        server.setSubsystemFactories(List.of(new SftpSubsystemFactory()));
        server.setFileSystemFactory(new VirtualFileSystemFactory(home));
        server.start();
        Files.createDirectories(home.resolve("exchange"));
        store = new SftpFileStore("Partner", new SftpSettings("127.0.0.1", server.getPort(), "demo", "demo-pass", null, null, null, "/exchange"));
    }

    @AfterAll
    static void stopServer() throws IOException {
        store.close();
        server.stop(true);
    }

    private static InputStream bytes(String text) {
        return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
    }

    private static String text(InputStream in) throws IOException {
        try (in) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    void theStoreIsRootedAtTheRemoteDirectory() throws IOException {
        store.write("/in/2026/orders.csv", bytes("id,qty\n1,2\n"), 11, "text/csv");
        store.mkdir("/out");
        assertTrue(Files.isRegularFile(home.resolve("exchange/in/2026/orders.csv")), "paths are relative to the configured directory");

        assertEquals(List.of("in", "out"), store.list("/").stream().map(FileEntry::name).toList());
        FileEntry orders = store.list("/in/2026").get(0);
        assertEquals("/in/2026/orders.csv", orders.path());
        assertEquals(11, orders.size());
        assertEquals("text/csv", orders.contentType());
        assertNotNull(orders.modified());
        assertEquals("id,qty\n1,2\n", text(store.read(orders.path())));
        assertTrue(store.stat("/in").dir());
        assertNull(store.stat("/missing"));
        assertEquals(List.of(), store.list("/missing"));
        assertThrows(NoSuchFileException.class, () -> store.read("/missing.txt"));
        assertThrows(IllegalArgumentException.class, () -> store.list("/../.."));

        store.rename("/in/2026", "archive");
        assertNull(store.stat("/in/2026"));
        assertEquals("orders.csv", store.list("/in/archive").get(0).name());

        store.delete("/in");
        assertFalse(Files.exists(home.resolve("exchange/in")), "a folder goes with everything in it");
        assertEquals(List.of("out"), store.list("/").stream().map(FileEntry::name).toList());
        assertThrows(IllegalArgumentException.class, () -> store.delete("/"));
        assertTrue(Files.isDirectory(home.resolve("exchange")), "the root itself stays");
    }

    @Test
    void aDroppedSessionIsReopened() throws IOException {
        store.close();
        store.mkdir("/again");
        assertNotNull(store.stat("/again"));
    }

}
