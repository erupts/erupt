package xyz.erupt.k8s_dashboard.terminal;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * One-time tickets for the terminal socket. The page that opens the socket was rendered behind the
 * erupt permission check; the ticket carries that decision (which pod, which container, what mode,
 * whose session) so the socket never takes a pod name from the browser.
 *
 * @author YuePeng
 */
@Service
public class K8sTerminalTicketService {

    private static final long TTL_MILLIS = 60_000;

    private final SecureRandom random = new SecureRandom();

    private final Map<String, Ticket> tickets = new ConcurrentHashMap<>();

    public enum Mode {
        EXEC, LOGS
    }

    // context: the kubeconfig context picked when the page rendered; the socket has no request to ask again
    public record Ticket(String token, Class<?> eruptClass, String context, String namespace, String pod, String container,
                         Mode mode, long expireAt) {
    }

    public String issue(String token, Class<?> eruptClass, String context, String namespace, String pod, String container, Mode mode) {
        long now = System.currentTimeMillis();
        tickets.values().removeIf(it -> it.expireAt() < now);
        byte[] bytes = new byte[24];
        random.nextBytes(bytes);
        String id = HexFormat.of().formatHex(bytes);
        tickets.put(id, new Ticket(token, eruptClass, context, namespace, pod, container, mode, now + TTL_MILLIS));
        return id;
    }

    // Removed on first use, expired or not
    public Ticket consume(String id) {
        if (null == id) return null;
        Ticket ticket = tickets.remove(id);
        return null == ticket || ticket.expireAt() < System.currentTimeMillis() ? null : ticket;
    }

}
