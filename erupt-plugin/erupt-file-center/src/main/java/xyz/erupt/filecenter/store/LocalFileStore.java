package xyz.erupt.filecenter.store;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * A directory on this server's disk.
 *
 * @author YuePeng
 */
public class LocalFileStore implements FileStore {

    private final String name;

    private final Path root;

    private final String publicBase;

    /**
     * @param publicBase a URL prefix under which the directory is already served, or null
     */
    public LocalFileStore(String name, Path root, String publicBase) {
        this.name = name;
        this.root = root.toAbsolutePath().normalize();
        this.publicBase = publicBase;
    }

    @Override
    public String name() {
        return name;
    }

    public Path root() {
        return root;
    }

    private Path resolve(String path) {
        Path target = root.resolve(FileStore.normalize(path).substring(1)).normalize();
        if (!target.startsWith(root)) throw new IllegalArgumentException("Illegal path: " + path);
        return target;
    }

    private FileEntry entry(Path file) throws IOException {
        BasicFileAttributes attributes = Files.readAttributes(file, BasicFileAttributes.class);
        String path = "/" + root.relativize(file).toString().replace('\\', '/');
        String name = file.getFileName().toString();
        return new FileEntry(name, path, attributes.isDirectory(), attributes.isDirectory() ? 0 : attributes.size(),
                attributes.lastModifiedTime().toMillis(), attributes.isDirectory() ? null : contentType(name));
    }

    static String contentType(String name) {
        String type = URLConnection.guessContentTypeFromName(name);
        return null == type ? "application/octet-stream" : type;
    }

    @Override
    public List<FileEntry> list(String dir) throws IOException {
        Path target = this.resolve(dir);
        if (!Files.isDirectory(target)) return List.of();
        List<FileEntry> entries = new ArrayList<>();
        try (Stream<Path> children = Files.list(target)) {
            for (Path child : children.toList()) entries.add(this.entry(child));
        }
        entries.sort(Comparator.comparing((FileEntry it) -> !it.dir()).thenComparing(it -> it.name().toLowerCase()));
        return entries;
    }

    @Override
    public FileEntry stat(String path) throws IOException {
        Path target = this.resolve(path);
        return Files.exists(target) ? this.entry(target) : null;
    }

    @Override
    public InputStream read(String path) throws IOException {
        Path target = this.resolve(path);
        if (!Files.isRegularFile(target)) throw new NoSuchFileException(path);
        return Files.newInputStream(target);
    }

    @Override
    public void write(String path, InputStream content, long size, String contentType) throws IOException {
        Path target = this.resolve(path);
        Files.createDirectories(target.getParent());
        Files.copy(content, target, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public void mkdir(String path) throws IOException {
        Files.createDirectories(this.resolve(path));
    }

    @Override
    public void rename(String path, String newName) throws IOException {
        Path source = this.resolve(path);
        Files.move(source, this.resolve(FileStore.join(FileStore.parentOf(path), newName)));
    }

    @Override
    public void delete(String path) throws IOException {
        Path target = this.resolve(path);
        if (target.equals(root)) throw new IllegalArgumentException("Not the root");
        if (!Files.exists(target)) return;
        try (Stream<Path> tree = Files.walk(target)) {
            for (Path each : tree.sorted(Comparator.reverseOrder()).toList()) Files.delete(each);
        }
    }

    @Override
    public String publicUrl(String path) {
        return null == publicBase ? null : publicBase + FileStore.normalize(path);
    }

}
