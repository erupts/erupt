package xyz.erupt.filecenter.store;

import com.jcraft.jsch.*;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

/**
 * A directory on an SFTP server. One SSH session is kept open and reconnected when it drops;
 * every operation runs on a channel of its own, so reads can stream while listings go on.
 *
 * @author YuePeng
 */
public class SftpFileStore implements FileStore {

    private static final int CONNECT_TIMEOUT_MS = 10_000;

    private final String name;

    private final SftpSettings prop;

    private Session session;

    public SftpFileStore(String name, SftpSettings prop) {
        this.name = name;
        this.prop = prop;
    }

    @Override
    public String name() {
        return name;
    }

    private synchronized Session session() throws JSchException {
        if (null == session || !session.isConnected()) {
            JSch jsch = new JSch();
            if (!prop.knownHosts().isEmpty()) jsch.setKnownHosts(prop.knownHosts());
            if (!prop.privateKey().isEmpty()) {
                byte[] passphrase = prop.passphrase().isEmpty() ? null : prop.passphrase().getBytes(StandardCharsets.UTF_8);
                if (prop.privateKey().startsWith("-----BEGIN")) {
                    jsch.addIdentity(name, prop.privateKey().getBytes(StandardCharsets.UTF_8), null, passphrase);
                } else {
                    jsch.addIdentity(prop.privateKey(), passphrase);
                }
            }
            Session ssh = jsch.getSession(prop.username(), prop.host(), prop.port());
            ssh.setConfig("StrictHostKeyChecking", prop.knownHosts().isEmpty() ? "no" : "yes");
            ssh.setConfig("PreferredAuthentications", prop.privateKey().isEmpty() ? "password,keyboard-interactive" : "publickey");
            if (!prop.password().isEmpty()) ssh.setPassword(prop.password());
            ssh.setServerAliveInterval(30_000);
            ssh.connect(CONNECT_TIMEOUT_MS);
            session = ssh;
        }
        return session;
    }

    private ChannelSftp channel() throws IOException {
        try {
            ChannelSftp channel = (ChannelSftp) this.session().openChannel("sftp");
            channel.connect(CONNECT_TIMEOUT_MS);
            return channel;
        } catch (JSchException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    @FunctionalInterface
    private interface SftpCall<T> {
        T call(ChannelSftp channel) throws SftpException, IOException;
    }

    private <T> T sftp(SftpCall<T> call) throws IOException {
        ChannelSftp channel = this.channel();
        try {
            return call.call(channel);
        } catch (SftpException e) {
            throw new IOException(e.getMessage(), e);
        } finally {
            channel.disconnect();
        }
    }

    // "/a/b" -> "<root>/a/b"
    private String remote(String path) {
        String relative = FileStore.normalize(path);
        if ("/".equals(prop.root())) return relative;
        return "/".equals(relative) ? prop.root() : prop.root() + relative;
    }

    private static FileEntry entry(String path, String name, SftpATTRS attrs) {
        boolean dir = attrs.isDir();
        return new FileEntry(name, path, dir, dir ? 0 : attrs.getSize(), attrs.getMTime() * 1000L, dir ? null : LocalFileStore.contentType(name));
    }

    private static boolean missing(SftpException e) {
        return ChannelSftp.SSH_FX_NO_SUCH_FILE == e.id;
    }

    @Override
    public List<FileEntry> list(String dir) throws IOException {
        String parent = FileStore.normalize(dir);
        return this.sftp(channel -> {
            List<FileEntry> entries = new ArrayList<>();
            Vector<ChannelSftp.LsEntry> rows;
            try {
                rows = channel.ls(this.remote(parent));
            } catch (SftpException e) {
                if (missing(e)) return entries;
                throw e;
            }
            for (ChannelSftp.LsEntry row : rows) {
                if (".".equals(row.getFilename()) || "..".equals(row.getFilename())) continue;
                entries.add(entry(FileStore.join(parent, row.getFilename()), row.getFilename(), row.getAttrs()));
            }
            entries.sort(Comparator.comparing((FileEntry it) -> !it.dir()).thenComparing(it -> it.name().toLowerCase()));
            return entries;
        });
    }

    @Override
    public FileEntry stat(String path) throws IOException {
        String normal = FileStore.normalize(path);
        if ("/".equals(normal)) return new FileEntry("", "/", true, 0, null, null);
        return this.sftp(channel -> {
            try {
                return entry(normal, FileStore.nameOf(normal), channel.stat(this.remote(normal)));
            } catch (SftpException e) {
                if (missing(e)) return null;
                throw e;
            }
        });
    }

    /** The channel stays open until the stream is closed. */
    @Override
    public InputStream read(String path) throws IOException {
        ChannelSftp channel = this.channel();
        try {
            InputStream stream = channel.get(this.remote(path));
            return new FilterInputStream(stream) {
                @Override
                public void close() throws IOException {
                    try {
                        super.close();
                    } finally {
                        channel.disconnect();
                    }
                }
            };
        } catch (SftpException e) {
            channel.disconnect();
            if (missing(e)) throw new NoSuchFileException(path);
            throw new IOException(e.getMessage(), e);
        }
    }

    private void mkdirs(ChannelSftp channel, String path) throws SftpException {
        StringBuilder current = new StringBuilder("/".equals(prop.root()) ? "" : prop.root());
        for (String part : FileStore.normalize(path).split("/")) {
            if (part.isEmpty()) continue;
            current.append('/').append(part);
            try {
                channel.stat(current.toString());
            } catch (SftpException e) {
                if (!missing(e)) throw e;
                channel.mkdir(current.toString());
            }
        }
    }

    @Override
    public void write(String path, InputStream content, long size, String contentType) throws IOException {
        this.sftp(channel -> {
            this.mkdirs(channel, FileStore.parentOf(path));
            channel.put(content, this.remote(path));
            return null;
        });
    }

    @Override
    public void mkdir(String path) throws IOException {
        this.sftp(channel -> {
            this.mkdirs(channel, path);
            return null;
        });
    }

    @Override
    public void rename(String path, String newName) throws IOException {
        String target = FileStore.join(FileStore.parentOf(path), newName);
        this.sftp(channel -> {
            channel.rename(this.remote(path), this.remote(target));
            return null;
        });
    }

    private void remove(ChannelSftp channel, String remote) throws SftpException {
        SftpATTRS attrs;
        try {
            attrs = channel.lstat(remote);
        } catch (SftpException e) {
            if (missing(e)) return;
            throw e;
        }
        if (attrs.isDir()) {
            for (ChannelSftp.LsEntry row : channel.ls(remote)) {
                if (".".equals(row.getFilename()) || "..".equals(row.getFilename())) continue;
                this.remove(channel, remote + "/" + row.getFilename());
            }
            channel.rmdir(remote);
        } else {
            channel.rm(remote);
        }
    }

    @Override
    public void delete(String path) throws IOException {
        if ("/".equals(FileStore.normalize(path))) throw new IllegalArgumentException("Not the root");
        this.sftp(channel -> {
            this.remove(channel, this.remote(path));
            return null;
        });
    }

    public synchronized void close() {
        if (null != session) session.disconnect();
        session = null;
    }

}
