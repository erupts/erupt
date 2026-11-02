package xyz.erupt.filecenter.store;

/**
 * What an SFTP store needs: the host, who to log in as with a password or a private key, and the
 * remote directory that becomes the store's root.
 * <p>
 * {@code privateKey} is PEM text or the path of a key file. With {@code knownHosts} set the server's
 * key must be in that file; without it any host key is accepted.
 *
 * @author YuePeng
 */
public record SftpSettings(String host, int port, String username, String password, String privateKey,
                           String passphrase, String knownHosts, String root) {

    public SftpSettings {
        port = port <= 0 ? 22 : port;
        password = null == password ? "" : password;
        privateKey = null == privateKey ? "" : privateKey.trim();
        passphrase = null == passphrase ? "" : passphrase;
        knownHosts = null == knownHosts ? "" : knownHosts.trim();
        root = FileStore.normalize(root);
    }

}
