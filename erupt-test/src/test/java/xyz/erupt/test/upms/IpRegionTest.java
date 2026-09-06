package xyz.erupt.test.upms;

import org.junit.jupiter.api.Test;
import xyz.erupt.upms.prop.EruptUpmsProp;
import xyz.erupt.upms.util.IpUtil;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * erupt-upms ships an ip2region database so a deployment resolves regions with no
 * network access and no prepared file. An external path still wins when configured,
 * which is how a newer or a v6 database is supplied, and an unusable one falls back
 * to the bundled copy instead of losing the feature for the whole JVM. Nothing here
 * may reach the network: the module has no download path and must not grow one back.
 *
 * @author YuePeng
 */
class IpRegionTest {

    /** Region strings are locale-dependent, so assert their shape rather than their words */
    private static void assertResolved(String region) {
        assertNotNull(region);
        assertFalse(region.isEmpty(), "the bundled database must resolve a public address");
        assertTrue(region.split("\\|").length >= 4, "unexpected segment layout: " + region);
    }

    private static EruptUpmsProp.Ip2Region prop(String path) {
        EruptUpmsProp.Ip2Region p = new EruptUpmsProp.Ip2Region();
        p.setPath(path);
        IpUtil.init(p);
        return p;
    }

    @Test
    void theDefaultConfigurationNeedsNoFileAndNoSetup() {
        // What every deployment gets out of the box: an empty path, nothing prepared
        prop("");
        assertResolved(IpUtil.getCityInfo("114.114.114.114"));
    }

    @Test
    void noBackgroundDownloadIsEverStarted() {
        prop(new File("no-such-dir", "absent.xdb").getAbsolutePath());
        IpUtil.getCityInfo("114.114.114.114");
        // The module resolves offline by design; a thread reaching out would undo that
        assertTrue(Thread.getAllStackTraces().keySet().stream()
                        .noneMatch(t -> t.getName().toLowerCase().contains("ip2region")),
                "no ip2region background thread may exist");
    }

    @Test
    void bundledDatabaseResolvesWithoutNetworkOrExternalFile() {
        prop(new File("no-such-dir", "absent.xdb").getAbsolutePath());
        assertResolved(IpUtil.getCityInfo("114.114.114.114"));
        assertResolved(IpUtil.getCityInfo("8.8.8.8"));
        assertNotEquals(IpUtil.getCityInfo("114.114.114.114"), IpUtil.getCityInfo("8.8.8.8"),
                "different networks must not resolve to the same region");
    }

    @Test
    void blankAndMalformedInputIsRejectedQuietly() {
        prop(new File("no-such-dir", "absent.xdb").getAbsolutePath());
        // ip2region's parser accepts all of these and answers with a bogus "Reserved"
        // region, and getIpAddr can return exactly such values from a proxy header
        for (String junk : new String[]{null, "", "not-an-ip", "unknown", "1.2.3", "999.1.1.1", "10.0.0.1.5", "1.2.3."}) {
            assertEquals("", IpUtil.getCityInfo(junk), "must not invent a region for " + junk);
        }
    }

    @Test
    void ipv4AndIpv6LiteralsAreRecognized() {
        for (String ip : new String[]{"0.0.0.0", "255.255.255.255", "8.8.8.8", "::1", "2001:db8::1", "::ffff:8.8.8.8"}) {
            assertTrue(IpUtil.isIpLiteral(ip), ip + " should be accepted");
        }
        // A zone-scoped link-local address is rejected on purpose: it carries no region,
        // and letting it through only produces a misleading "Reserved" answer
        for (String junk : new String[]{"", "unknown", "example.com", "256.1.1.1", "1.2.3", "1.2.3.4.5", "fe80::1%eth0"}) {
            assertFalse(IpUtil.isIpLiteral(junk), junk + " should be rejected");
        }
    }

    @Test
    void anUnusableExternalFileFallsBackToTheBundledCopy() throws Exception {
        // A truncated file is a configuration mistake, not a reason to stop resolving
        Path truncated = Files.createTempFile("ip2region-truncated", ".xdb");
        Files.write(truncated, new byte[64]);
        try {
            prop(truncated.toAbsolutePath().toString());
            assertResolved(IpUtil.getCityInfo("114.114.114.114"));
        } finally {
            Files.deleteIfExists(truncated);
        }
    }

    @Test
    void lookupIsDisabledWhenTurnedOff() {
        EruptUpmsProp.Ip2Region p = prop(new File("no-such-dir", "absent.xdb").getAbsolutePath());
        p.setEnable(false);
        IpUtil.init(p);
        assertEquals("", IpUtil.getCityInfo("114.114.114.114"));
    }

}
