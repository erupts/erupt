package xyz.erupt.webscoket.channel;

import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Per-connection async writer. The websocket spec forbids concurrent sends on
 * one session, so messages are queued and drained one at a time via completion
 * callbacks — business threads never block on a slow client.
 *
 * @author YuePeng
 * date 2026/9/1
 */
@Slf4j
public class EruptWsSender {

    private static final long SEND_TIMEOUT_MILLIS = 10_000L;

    // backpressure: cap the queue so a dead client can't pile up messages in memory
    private static final int MAX_QUEUE_SIZE = 512;

    private final Session session;

    private final Queue<String> queue = new ConcurrentLinkedQueue<>();

    private final AtomicInteger queueSize = new AtomicInteger();

    private final AtomicBoolean sending = new AtomicBoolean();

    public EruptWsSender(Session session) {
        this.session = session;
        session.getAsyncRemote().setSendTimeout(SEND_TIMEOUT_MILLIS);
    }

    public void send(String text) {
        if (queueSize.incrementAndGet() > MAX_QUEUE_SIZE) {
            queueSize.decrementAndGet();
            log.warn("[websocket] Send queue overflow, message dropped: id={}", session.getId());
            return;
        }
        queue.offer(text);
        this.drain();
    }

    private void drain() {
        if (!sending.compareAndSet(false, true)) return;
        String next = queue.poll();
        if (next == null) {
            sending.set(false);
            // recheck: a message may have been offered between poll and release
            if (!queue.isEmpty()) this.drain();
            return;
        }
        queueSize.decrementAndGet();
        try {
            session.getAsyncRemote().sendText(next, result -> {
                sending.set(false);
                if (!result.isOK()) {
                    log.warn("[websocket] Send failed: id={}, {}", session.getId(), String.valueOf(result.getException()));
                }
                this.drain();
            });
        } catch (Exception e) {
            sending.set(false);
            log.warn("[websocket] Send error: id={}, {}", session.getId(), e.getMessage());
        }
    }

}
