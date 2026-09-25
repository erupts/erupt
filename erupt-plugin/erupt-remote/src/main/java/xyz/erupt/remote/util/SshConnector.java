package xyz.erupt.remote.util;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.remote.model.RemoteHost;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Opens an authenticated SSH session towards a {@link RemoteHost}. Shared by the interactive shell and the
 * SFTP file panel so both apply the same credentials, host-key policy and timeouts.
 *
 * @author YuePeng
 */
public final class SshConnector {

    private static final String KNOWN_HOSTS_FILE = "remote_known_hosts";

    private SshConnector() {
    }

    /**
     * @param password   login password, or the key passphrase when {@code privateKey} is set; may be null
     * @param privateKey PEM private key; takes precedence over the password when present
     */
    public static Session connect(RemoteHost host, String password, String privateKey, int connectTimeoutMs)
            throws JSchException, IOException {
        JSch jsch = new JSch();
        jsch.setKnownHosts(knownHostsFile().toString());
        jsch.setHostKeyRepository(new TofuHostKeyRepository(jsch.getHostKeyRepository()));
        if (privateKey != null) {
            jsch.addIdentity(host.getName(), privateKey.getBytes(StandardCharsets.UTF_8), null,
                    password == null ? null : password.getBytes(StandardCharsets.UTF_8));
        }
        Session ssh = jsch.getSession(host.getUsername(), host.getHost(), host.getPort());
        ssh.setConfig("StrictHostKeyChecking", "yes");
        ssh.setConfig("PreferredAuthentications", privateKey != null ? "publickey" : "password,keyboard-interactive");
        ssh.setServerAliveInterval(30_000);
        if (privateKey == null && password != null) {
            ssh.setPassword(password);
            ssh.setUserInfo(new PasswordOnly(password));
        }
        ssh.connect(connectTimeoutMs);
        return ssh;
    }

    /** Whether a JSch failure is a credential or host-key refusal rather than a network problem. */
    public static boolean isAuthFailure(JSchException e) {
        String m = e.getMessage() == null ? e.toString() : e.getMessage();
        return m.contains("Auth fail") || m.contains("Auth cancel") || m.contains("USERAUTH")
                || m.contains("HostKey") || m.contains("reject");
    }

    private static Path knownHostsFile() throws IOException {
        Path file = Paths.get(EruptConst.ERUPT_DIR_PATH, KNOWN_HOSTS_FILE);
        if (!Files.exists(file)) {
            Files.createDirectories(file.getParent());
            Files.createFile(file);
        }
        return file;
    }

    /** Answers password and keyboard-interactive prompts non-interactively; never shows anything. */
    private record PasswordOnly(String pwd) implements UserInfo, UIKeyboardInteractive {
        @Override
        public String getPassphrase() {
            return pwd;
        }

        @Override
        public String getPassword() {
            return pwd;
        }

        @Override
        public boolean promptPassword(String message) {
            return true;
        }

        @Override
        public boolean promptPassphrase(String message) {
            return true;
        }

        @Override
        public boolean promptYesNo(String message) {
            return false;
        }

        @Override
        public void showMessage(String message) {
        }

        @Override
        public String[] promptKeyboardInteractive(String destination, String name, String instruction, String[] prompt, boolean[] echo) {
            String[] answers = new String[prompt.length];
            Arrays.fill(answers, pwd);
            return answers;
        }
    }
}
