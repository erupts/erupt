package xyz.erupt.upms.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.xdb.Header;
import org.lionsoul.ip2region.xdb.Searcher;
import org.lionsoul.ip2region.xdb.Version;
import xyz.erupt.upms.prop.EruptUpmsProp;

import java.io.File;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

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

    // ---- ip2region (xdb v2 format, vector-index cached, segments read from disk on demand) ----

    private static final long DOWNLOAD_RETRY_INTERVAL_MS = Duration.ofMinutes(10).toMillis();

    private static volatile EruptUpmsProp.Ip2Region prop;

    private static volatile Searcher searcher;

    // Set when the xdb file exists but cannot be used; avoids re-parsing a broken file on every lookup
    private static volatile boolean broken;

    private static final AtomicBoolean downloading = new AtomicBoolean();

    private static volatile long nextDownloadAt;

    public static void init(EruptUpmsProp.Ip2Region ip2Region) {
        prop = ip2Region;
    }

    /**
     * Resolve the region of an IP address, e.g. {@code China|0|Beijing|Beijing|Aliyun}.
     * Returns an empty string when the xdb is disabled, not yet available, or the IP cannot be resolved.
     */
    public static String getCityInfo(String ip) {
        if (ip == null || ip.isEmpty()) return "";
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

    private static Searcher searcher() {
        if (searcher != null) return searcher;
        if (prop == null || !prop.isEnable() || broken) return null;
        synchronized (IpUtil.class) {
            if (searcher != null) return searcher;
            File xdb = new File(prop.getPath());
            if (!xdb.isFile()) {
                download(xdb);
                return null;
            }
            try {
                Header header = Searcher.loadHeaderFromFile(xdb);
                Searcher.verify(header, xdb.length());
                Version version = Version.fromHeader(header);
                searcher = Searcher.newWithVectorIndex(version, xdb, Searcher.loadVectorIndexFromFile(xdb));
                log.info("ip2region loaded {} ({})", xdb.getAbsolutePath(), version.name);
            } catch (Exception e) {
                broken = true;
                log.warn("ip2region xdb unusable, region lookup disabled: {} ({})", xdb.getAbsolutePath(), e.getMessage());
            }
            return searcher;
        }
    }

    private static void download(File xdb) {
        String url = prop.getDownloadUrl();
        if (url == null || url.isEmpty() || System.currentTimeMillis() < nextDownloadAt) return;
        if (!downloading.compareAndSet(false, true)) return;
        Thread thread = new Thread(() -> {
            Path target = xdb.toPath().toAbsolutePath();
            Path tmp = target.resolveSibling(target.getFileName() + ".tmp");
            try {
                Files.createDirectories(target.getParent());
                log.info("ip2region xdb missing, downloading {} -> {}", url, target);
                HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL)
                        .connectTimeout(Duration.ofSeconds(15)).build();
                HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofMinutes(5)).GET().build();
                HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
                if (response.statusCode() != 200) {
                    throw new IllegalStateException("HTTP " + response.statusCode());
                }
                try (InputStream in = response.body()) {
                    Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
                }
                // Reject truncated / non-xdb downloads before publishing the file
                Searcher.verifyFromFile(tmp.toFile());
                Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                log.info("ip2region xdb ready {}", target);
            } catch (Exception e) {
                nextDownloadAt = System.currentTimeMillis() + DOWNLOAD_RETRY_INTERVAL_MS;
                log.warn("ip2region xdb download failed, retry in 10 minutes ({}): {}", url, e.getMessage());
                try {
                    Files.deleteIfExists(tmp);
                } catch (Exception ignored) {
                }
            } finally {
                downloading.set(false);
            }
        }, "ip2region-download");
        thread.setDaemon(true);
        thread.start();
    }

}
