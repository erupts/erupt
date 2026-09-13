package xyz.erupt.remote.util;

import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.UserInfo;
import lombok.extern.slf4j.Slf4j;

/**
 * Trust-on-first-use host key policy: unknown hosts are recorded and accepted,
 * a host whose key changed is rejected (the delegate reports CHANGED).
 *
 * @author YuePeng
 */
@Slf4j
public class TofuHostKeyRepository implements HostKeyRepository {

    private final HostKeyRepository delegate;

    public TofuHostKeyRepository(HostKeyRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public int check(String host, byte[] key) {
        int result = delegate.check(host, key);
        if (result == NOT_INCLUDED) {
            try {
                delegate.add(new HostKey(host, key), null);
                log.info("[erupt-remote] Recorded new SSH host key for {}", host);
                return OK;
            } catch (JSchException e) {
                log.warn("[erupt-remote] Cannot record host key for {}: {}", host, e.getMessage());
                return NOT_INCLUDED;
            }
        }
        if (result == CHANGED) {
            log.warn("[erupt-remote] SSH host key for {} has CHANGED, refusing to connect", host);
        }
        return result;
    }

    @Override
    public void add(HostKey hostkey, UserInfo ui) {
        delegate.add(hostkey, ui);
    }

    @Override
    public void remove(String host, String type) {
        delegate.remove(host, type);
    }

    @Override
    public void remove(String host, String type, byte[] key) {
        delegate.remove(host, type, key);
    }

    @Override
    public String getKnownHostsRepositoryID() {
        return delegate.getKnownHostsRepositoryID();
    }

    @Override
    public HostKey[] getHostKey() {
        return delegate.getHostKey();
    }

    @Override
    public HostKey[] getHostKey(String host, String type) {
        return delegate.getHostKey(host, type);
    }
}
