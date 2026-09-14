package xyz.erupt.remote.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SftpPathsTest {

    @Test
    void normalizeCollapsesDotsAndSlashes() {
        assertEquals("/", SftpPaths.normalize(null));
        assertEquals("/", SftpPaths.normalize(""));
        assertEquals("/", SftpPaths.normalize("/"));
        assertEquals("/home/erupt", SftpPaths.normalize("/home//erupt/"));
        assertEquals("/home", SftpPaths.normalize("/home/erupt/./.."));
        assertEquals("/", SftpPaths.normalize("/../../.."));
        assertEquals("/etc/hosts", SftpPaths.normalize("/tmp/../etc/./hosts"));
    }

    @Test
    void normalizeRejectsRelativeAndBinaryPaths() {
        assertThrows(IllegalArgumentException.class, () -> SftpPaths.normalize("home/erupt"));
        assertThrows(IllegalArgumentException.class, () -> SftpPaths.normalize("../etc"));
        assertThrows(IllegalArgumentException.class, () -> SftpPaths.normalize("/etc/\0passwd"));
    }

    @Test
    void fileNameMustBeBare() {
        assertEquals("a.txt", SftpPaths.fileName("a.txt"));
        assertEquals("with space.tar.gz", SftpPaths.fileName("with space.tar.gz"));
        for (String bad : new String[]{null, "", "  ", ".", "..", "a/b", "a\\b", "../x", "a\0b"}) {
            assertThrows(IllegalArgumentException.class, () -> SftpPaths.fileName(bad), String.valueOf(bad));
        }
    }
}
