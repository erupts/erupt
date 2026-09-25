package xyz.erupt.remote.service;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.remote.config.EruptRemoteProp;
import xyz.erupt.remote.model.RemoteHost;
import xyz.erupt.remote.model.SftpEntry;
import xyz.erupt.remote.util.RemoteCrypto;
import xyz.erupt.remote.util.SftpPaths;
import xyz.erupt.remote.util.SshConnector;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.function.LongFunction;

/**
 * File transfer for SSH hosts over the SFTP subsystem of the same connection the terminal uses.
 * <p>
 * Every operation opens its own short-lived session and closes it when done: a transfer is never tied to the
 * lifetime of a terminal WebSocket, and a dropped terminal never kills a running download. Remote permissions
 * are those of the host's SSH login user, exactly as in the shell.
 *
 * @author YuePeng
 */
@Slf4j
@Service
public class RemoteSftpService {

    @Resource
    private RemoteCrypto remoteCrypto;

    @Resource
    private EruptRemoteProp prop;

    /** Absolute path of the login user's home directory. */
    public String home(RemoteHost host) {
        return run(host, ChannelSftp::getHome);
    }

    public List<SftpEntry> list(RemoteHost host, String path) {
        String dir = normalize(path);
        return run(host, sftp -> {
            @SuppressWarnings("unchecked")
            Vector<ChannelSftp.LsEntry> raw = sftp.ls(dir);
            List<SftpEntry> entries = new ArrayList<>(raw.size());
            for (ChannelSftp.LsEntry e : raw) {
                String name = e.getFilename();
                if (".".equals(name) || "..".equals(name)) continue;
                SftpATTRS attrs = e.getAttrs();
                boolean directory = attrs.isDir();
                // a symlink is shown as what it points at, so the browser can descend into linked directories
                if (attrs.isLink()) {
                    try {
                        directory = sftp.stat(dir + "/" + name).isDir();
                    } catch (SftpException ignored) {
                        // dangling link: keep it listed as a plain file
                    }
                }
                entries.add(new SftpEntry(name, directory, directory ? 0 : attrs.getSize(),
                        attrs.getMTime() * 1000L, attrs.getPermissionsString()));
            }
            entries.sort(Comparator.comparing(SftpEntry::directory).reversed()
                    .thenComparing(SftpEntry::name, String.CASE_INSENSITIVE_ORDER));
            return entries;
        });
    }

    /**
     * Streams a remote file into the stream {@code sink} hands back for its size, so the caller can announce the
     * length before the first byte. One connection serves both the stat and the transfer. Refuses directories.
     */
    public void download(RemoteHost host, String path, LongFunction<OutputStream> sink) {
        String file = normalize(path);
        run(host, sftp -> {
            SftpATTRS attrs = sftp.stat(file);
            if (attrs.isDir()) throw new EruptWebApiRuntimeException(I18nTranslate.$translate("remote.sftp_not_a_file"));
            sftp.get(file, sink.apply(attrs.getSize()));
            return null;
        });
    }

    /** Streams {@code in} into {@code dir/name}, replacing an existing file of that name. */
    public void upload(RemoteHost host, String dir, String name, InputStream in) {
        String target = normalize(dir) + "/" + requireFileName(name);
        run(host, sftp -> {
            sftp.put(in, target, ChannelSftp.OVERWRITE);
            return null;
        });
    }

    public void mkdir(RemoteHost host, String dir, String name) {
        String target = normalize(dir) + "/" + requireFileName(name);
        run(host, sftp -> {
            sftp.mkdir(target);
            return null;
        });
    }

    /**
     * Removes one file or one <em>empty</em> directory. Never recursive: clearing a tree is a shell job, where
     * the user sees what they are typing.
     */
    public void delete(RemoteHost host, String path) {
        String target = normalize(path);
        if ("/".equals(target)) throw new EruptWebApiRuntimeException(I18nTranslate.$translate("remote.sftp_bad_path"));
        run(host, sftp -> {
            if (sftp.stat(target).isDir()) {
                sftp.rmdir(target);
            } else {
                sftp.rm(target);
            }
            return null;
        });
    }

    // ------------------------------------------------------------------ helpers

    public static String normalize(String path) {
        try {
            return SftpPaths.normalize(path);
        } catch (IllegalArgumentException e) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("remote.sftp_bad_path"));
        }
    }

    public static String requireFileName(String name) {
        try {
            return SftpPaths.fileName(name);
        } catch (IllegalArgumentException e) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("remote.sftp_bad_name"));
        }
    }

    private interface SftpAction<T> {
        T apply(ChannelSftp sftp) throws SftpException, IOException;
    }

    private <T> T run(RemoteHost host, SftpAction<T> action) {
        Session ssh = null;
        ChannelSftp sftp = null;
        try {
            int timeoutMs = prop.getConnectTimeoutSeconds() * 1000;
            ssh = SshConnector.connect(host, remoteCrypto.decrypt(host.getPassword()),
                    remoteCrypto.decrypt(host.getPrivateKey()), timeoutMs);
            sftp = (ChannelSftp) ssh.openChannel("sftp");
            sftp.connect(timeoutMs);
            return action.apply(sftp);
        } catch (JSchException e) {
            log.warn("[erupt-remote] SFTP to {}:{} failed: {}", host.getHost(), host.getPort(), e.toString());
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate(
                    SshConnector.isAuthFailure(e) ? "remote.sftp_auth_failed" : "remote.sftp_connect_failed"));
        } catch (SftpException e) {
            // the server's own words (No such file, Permission denied, ...) are the most useful message
            throw new EruptWebApiRuntimeException(e.getMessage());
        } catch (IOException e) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("remote.sftp_connect_failed"));
        } finally {
            if (sftp != null) sftp.disconnect();
            if (ssh != null) ssh.disconnect();
        }
    }
}
