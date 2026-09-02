package xyz.erupt.upms.telemetry;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the telemetry host probes. Both parsers only ever run on Linux, so they are
 * covered here with captured os-release / cgroup samples instead of the live filesystem.
 * <p>
 * Lives in the same package as {@link EruptTelemetry} to reach its package private parsers.
 */
public class TelemetryTest {

    private static List<String> lines(String... lines) {
        return Arrays.asList(lines);
    }

    @Test
    public void parseOsReleaseStripsQuotesAndLowercases() {
        assertEquals("kylin", EruptTelemetry.parseOsRelease(lines(
                "NAME=\"Kylin Linux Advanced Server\"", "ID=\"kylin\"", "VERSION_ID=\"V10\"")));
        assertEquals("uos", EruptTelemetry.parseOsRelease(lines("ID=\"uos\"")));
        // openEuler ships a mixed case id, grouping in SQL needs a stable case
        assertEquals("openeuler", EruptTelemetry.parseOsRelease(lines("ID=\"openEuler\"")));
    }

    @Test
    public void parseOsReleaseAcceptsUnquotedId() {
        assertEquals("ubuntu", EruptTelemetry.parseOsRelease(lines(
                "NAME=\"Ubuntu\"", "ID=ubuntu", "ID_LIKE=debian")));
    }

    @Test
    public void parseOsReleaseIgnoresLookalikeKeys() {
        // VERSION_ID and ID_LIKE both end in 'ID' or start with 'ID', neither may win
        assertEquals("centos", EruptTelemetry.parseOsRelease(lines(
                "VERSION_ID=\"7\"", "ID_LIKE=\"rhel fedora\"", "ID=\"centos\"")));
    }

    @Test
    public void parseOsReleaseReturnsNullWhenAbsent() {
        assertNull(EruptTelemetry.parseOsRelease(lines("NAME=\"Something\"", "VERSION_ID=\"1\"")));
        assertNull(EruptTelemetry.parseOsRelease(lines("ID=", "ID=\"\"")));
        assertNull(EruptTelemetry.parseOsRelease(Collections.emptyList()));
    }

    @Test
    public void containerCgroupDetectsDockerAndKubernetes() {
        assertTrue(EruptTelemetry.isContainerCgroup(
                "12:cpu,cpuacct:/docker/3f2b1a9c4d5e6f70819273645566778899aabbccddeeff0011223344556677"));
        assertTrue(EruptTelemetry.isContainerCgroup(
                "11:memory:/kubepods/besteffort/pod9f8e7d6c/2a3b4c5d6e7f8091"));
        assertTrue(EruptTelemetry.isContainerCgroup(
                "0::/system.slice/containerd.service/kubepods-burstable.slice"));
    }

    @Test
    public void containerCgroupIgnoresBareMetal() {
        // pid 1 on a systemd host, and the cgroup v2 root a host process reports
        assertFalse(EruptTelemetry.isContainerCgroup("1:name=systemd:/init.scope"));
        assertFalse(EruptTelemetry.isContainerCgroup("0::/"));
        assertFalse(EruptTelemetry.isContainerCgroup(""));
        assertFalse(EruptTelemetry.isContainerCgroup(null));
    }

}
