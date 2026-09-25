package xyz.erupt.remote.model;

/**
 * One row of a remote directory listing.
 *
 * @param name      file name without directory
 * @param directory whether the entry (after following symlinks) is a directory
 * @param size      size in bytes, 0 for directories
 * @param mtime     last modification time, epoch millis
 * @param mode      permission string as {@code ls -l} prints it
 * @author YuePeng
 */
public record SftpEntry(String name, boolean directory, long size, long mtime, String mode) {
}
