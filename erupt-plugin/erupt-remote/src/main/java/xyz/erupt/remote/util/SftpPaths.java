package xyz.erupt.remote.util;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Pure path rules of the SFTP panel: the browser sends whatever it likes, the server only ever sees clean absolute
 * paths and bare file names. Violations throw {@link IllegalArgumentException} for the caller to translate.
 *
 * @author YuePeng
 */
public final class SftpPaths {

    private SftpPaths() {
    }

    /**
     * Resolves {@code .} and {@code ..} lexically and rejects anything that is not an absolute path, so the paths
     * shown in the browser and logged on the server are always canonical. Symlinks are left to the server.
     */
    public static String normalize(String path) {
        if (path == null || path.isEmpty()) return "/";
        if (path.indexOf('\0') >= 0 || !path.startsWith("/")) throw new IllegalArgumentException("path");
        Deque<String> parts = new ArrayDeque<>();
        for (String seg : path.split("/")) {
            if (seg.isEmpty() || ".".equals(seg)) continue;
            if ("..".equals(seg)) {
                parts.pollLast();
            } else {
                parts.addLast(seg);
            }
        }
        return parts.isEmpty() ? "/" : "/" + String.join("/", parts);
    }

    /** A bare file name: no separators, no traversal, nothing that would land outside the chosen directory. */
    public static String fileName(String name) {
        if (name == null || name.isBlank() || name.indexOf('/') >= 0 || name.indexOf('\\') >= 0
                || name.indexOf('\0') >= 0 || ".".equals(name) || "..".equals(name)) {
            throw new IllegalArgumentException("name");
        }
        return name;
    }
}
