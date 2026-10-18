package xyz.erupt.filecenter.store;

/**
 * One row of a listing. {@code path} is store-relative, starts with "/", and names a directory
 * without a trailing slash.
 *
 * @author YuePeng
 */
public record FileEntry(String name, String path, boolean dir, long size, Long modified, String contentType) {
}
