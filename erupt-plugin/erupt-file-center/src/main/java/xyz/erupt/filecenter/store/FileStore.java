package xyz.erupt.filecenter.store;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * One place files live, browsed by the file center. Paths are store-relative, "/" at the root;
 * a store validates them and refuses anything that climbs out.
 *
 * @author YuePeng
 */
public interface FileStore {

    String name();

    /** Directories first, then files, each group by name. */
    List<FileEntry> list(String dir) throws IOException;

    /** Null when nothing is there. */
    FileEntry stat(String path) throws IOException;

    InputStream read(String path) throws IOException;

    void write(String path, InputStream content, long size, String contentType) throws IOException;

    void mkdir(String path) throws IOException;

    /** Renames within the same directory. */
    void rename(String path, String newName) throws IOException;

    /** A directory goes with everything in it. */
    void delete(String path) throws IOException;

    /** Where a browser can load the file from without the file center, or null. */
    default String publicUrl(String path) {
        return null;
    }

    // ---- path helpers shared by the stores

    /** "/a/b" for any spelling; refuses "..", so no path climbs out of the store. */
    static String normalize(String path) {
        if (null == path || path.isBlank()) return "/";
        String clean = path.replace('\\', '/');
        StringBuilder out = new StringBuilder();
        for (String part : clean.split("/")) {
            if (part.isEmpty() || ".".equals(part)) continue;
            if ("..".equals(part)) throw new IllegalArgumentException("Illegal path: " + path);
            out.append('/').append(part);
        }
        return out.isEmpty() ? "/" : out.toString();
    }

    static String nameOf(String path) {
        String normal = normalize(path);
        return normal.substring(normal.lastIndexOf('/') + 1);
    }

    static String parentOf(String path) {
        String normal = normalize(path);
        int slash = normal.lastIndexOf('/');
        return slash <= 0 ? "/" : normal.substring(0, slash);
    }

    static String join(String dir, String name) {
        if (null == name || name.isBlank() || name.contains("/") || name.contains("\\") || "..".equals(name) || ".".equals(name)) {
            throw new IllegalArgumentException("Illegal name: " + name);
        }
        String parent = normalize(dir);
        return "/".equals(parent) ? "/" + name : parent + "/" + name;
    }

}
