package xyz.erupt.upms.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.xdb.Header;
import org.lionsoul.ip2region.xdb.LongByteArray;
import org.lionsoul.ip2region.xdb.Searcher;
import org.lionsoul.ip2region.xdb.Version;
import xyz.erupt.upms.prop.EruptUpmsProp;

import java.io.File;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.regex.Pattern;

/**
 * @author YuePeng
 * date 2018-12-24.
 */
@Slf4j
public class IpUtil {

    public static String getIpAddr(HttpServletRequest request) {
        try {
            String ip = request.getHeader("x-forwarded-for");
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
                if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
                    // Obtain the IP address of this machine from the network card.
                    InetAddress inet = InetAddress.getLocalHost();
                    ip = inet.getHostAddress();
                    // For the case where multiple proxies are used, the first IP represents the client's actual IP, and the multiple IPs are separated by ','.
                    // "***.***.***.***".length()
                    if (ip != null && ip.length() > 15) {
                        // = 15
                        if (ip.indexOf(',') > 0) {
                            ip = ip.substring(0, ip.indexOf(','));
                        }
                    }
                }
            }
            // "***.***.***.***".length()
            if (ip != null && ip.length() > 15) {
                // = 15
                if (ip.indexOf(',') > 0) {
                    ip = ip.substring(0, ip.indexOf(','));
                }
            }
            return ip;
        } catch (Exception e) {
            log.warn("get ip error {}", e.getMessage());
            return null;
        }
    }

    // ---- ip2region (xdb v2 format) ----

    // Bundled copy, loaded fully into memory when no external file is configured
    static final String CLASSPATH_XDB = "ip2region_v4.xdb";

    private static volatile EruptUpmsProp.Ip2Region prop;

    private static volatile Searcher searcher;

    // Set when neither the configured file nor the bundled copy is usable;
    // avoids re-parsing a broken database on every lookup
    private static volatile boolean broken;

    public static void init(EruptUpmsProp.Ip2Region ip2Region) {
        prop = ip2Region;
    }

    /**
     * Resolve the region of an IP address, e.g. {@code China|0|Beijing|Beijing|Aliyun}.
     * Returns an empty string when the xdb is disabled, not yet available, or the IP cannot be resolved.
     */
    public static String getCityInfo(String ip) {
        if (ip == null || !isIpLiteral(ip)) return "";
        Searcher s = searcher();
        if (s == null) return "";
        try {
            // Searcher is not thread safe (shared RandomAccessFile); lookups are a few page reads so a lock is cheap
            synchronized (s) {
                return s.search(ip);
            }
        } catch (Exception e) {
            log.debug("ip2region search failed for {}: {}", ip, e.getMessage());
            return "";
        }
    }

    private static final Pattern IPV4 = Pattern.compile("(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}");

    // Loose on purpose: it only has to reject hostnames and junk, the searcher parses the rest
    private static final Pattern IPV6 = Pattern.compile("[0-9A-Fa-f:]*:[0-9A-Fa-f:.]*");

    /**
     * ip2region's own parser is lenient enough to turn "unknown", "1.2.3" or a
     * hostname into a bogus "Reserved" region, and {@link #getIpAddr} can return
     * exactly those, so screen the address before looking it up. Public because
     * anything consuming {@link #getIpAddr} needs the same guard.
     */
    public static boolean isIpLiteral(String ip) {
        if (ip.isEmpty()) return false;
        return ip.indexOf(':') >= 0 ? IPV6.matcher(ip).matches() : IPV4.matcher(ip).matches();
    }

    private static Searcher searcher() {
        // Checked before the cached instance so disabling actually disables
        if (prop == null || !prop.isEnable() || broken) return null;
        if (searcher != null) return searcher;
        synchronized (IpUtil.class) {
            if (searcher != null) return searcher;
            String path = prop.getPath();
            File xdb = null == path || path.isBlank() ? null : new File(path);
            if (null != xdb && xdb.isFile()) {
                // An external file wins: it is how a deployment ships a newer or a v6 database
                try {
                    Header header = Searcher.loadHeaderFromFile(xdb);
                    Searcher.verify(header, xdb.length());
                    Version version = Version.fromHeader(header);
                    // Only the vector index is held; segments are read from disk per lookup
                    searcher = Searcher.newWithVectorIndex(version, xdb, Searcher.loadVectorIndexFromFile(xdb));
                    log.info("ip2region loaded {} ({})", xdb.getAbsolutePath(), version.name);
                    return searcher;
                } catch (Exception e) {
                    // Fall through to the bundled copy rather than losing region lookup for the
                    // whole JVM: a truncated or stale external file is a configuration mistake,
                    // and the warning names it. Only a missing bundled copy disables the feature
                    log.warn("ip2region xdb unusable, falling back to the bundled database: {} ({})",
                            xdb.getAbsolutePath(), e.getMessage());
                }
            }
            // Nothing configured, or what was configured is unusable: fall back to the copy
            // shipped inside the jar, so a deployment resolves regions with no setup at all
            try (InputStream in = IpUtil.class.getClassLoader().getResourceAsStream(CLASSPATH_XDB)) {
                if (null != in) {
                    LongByteArray content = Searcher.loadContentFromInputStream(in);
                    Header header = Searcher.loadHeaderFromBuffer(content);
                    Searcher.verify(header, content.length());
                    Version version = Version.fromHeader(header);
                    searcher = Searcher.newWithBuffer(version, content);
                    log.info("ip2region loaded from classpath {} ({})", CLASSPATH_XDB, version.name);
                    return searcher;
                }
            } catch (Exception e) {
                log.warn("ip2region classpath xdb unusable ({}): {}", CLASSPATH_XDB, e.getMessage());
            }
            // Only reachable when the bundled resource was stripped from the jar
            log.warn("ip2region database unavailable, region lookup disabled");
            broken = true;
            return null;
        }
    }


}
