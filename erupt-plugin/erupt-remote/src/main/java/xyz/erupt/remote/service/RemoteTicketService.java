package xyz.erupt.remote.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * One-time tickets that authorize a single WebSocket session towards one host.
 * A ticket is bound to the login token that requested it and expires after {@link #TTL_MS}.
 *
 * @author YuePeng
 */
@Component
public class RemoteTicketService {

    public record Ticket(Long hostId, String token, long expireAt) {
    }

    private static final long TTL_MS = 60_000L;

    private final SecureRandom random = new SecureRandom();

    private final Map<String, Ticket> tickets = new ConcurrentHashMap<>();

    public String issue(Long hostId, String token) {
        purgeExpired();
        byte[] raw = new byte[32];
        random.nextBytes(raw);
        String ticket = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        tickets.put(ticket, new Ticket(hostId, token, System.currentTimeMillis() + TTL_MS));
        return ticket;
    }

    /**
     * @return the ticket if it is valid for this token, otherwise {@code null}; the ticket is consumed either way
     */
    public Ticket consume(String ticket, String token) {
        if (ticket == null || token == null) return null;
        Ticket t = tickets.remove(ticket);
        if (t == null || t.expireAt() < System.currentTimeMillis() || !t.token().equals(token)) return null;
        return t;
    }

    private void purgeExpired() {
        long now = System.currentTimeMillis();
        tickets.entrySet().removeIf(e -> e.getValue().expireAt() < now);
    }
}
