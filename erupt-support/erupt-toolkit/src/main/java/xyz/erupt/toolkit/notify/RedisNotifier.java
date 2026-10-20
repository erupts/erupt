package xyz.erupt.toolkit.notify;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.prop.EruptProp;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * Cluster broadcast over Redis pub/sub, engaged only with {@code erupt.redis-session=true}: the
 * one place modules tell their other instances that something changed (erupt-job's schedules,
 * erupt-config's values). Without Redis sessions both calls do nothing.
 *
 * @author YuePeng
 */
@Slf4j
@Component
public class RedisNotifier {

    private final EruptProp eruptProp;

    // providers: Redis is only touched with Redis sessions on, and may be absent otherwise
    private final ObjectProvider<StringRedisTemplate> redisTemplate;

    private final ObjectProvider<RedisConnectionFactory> connectionFactory;

    public RedisNotifier(EruptProp eruptProp, ObjectProvider<StringRedisTemplate> redisTemplate,
                         ObjectProvider<RedisConnectionFactory> connectionFactory) {
        this.eruptProp = eruptProp;
        this.redisTemplate = redisTemplate;
        this.connectionFactory = connectionFactory;
    }

    public boolean enabled() {
        return eruptProp.isRedisSession();
    }

    /**
     * Sends the message as JSON once the current transaction commits, or right away outside one:
     * a listener reading the database must find the change, and a rolled back change must not be
     * announced at all. The message is serialized now, as the caller sees it.
     */
    public void publish(String channel, Object message) {
        if (!this.enabled()) return;
        String json = message instanceof String text ? text : GsonFactory.getGson().toJson(message);
        Runnable send = () -> redisTemplate.getObject().convertAndSend(channel, json);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    send.run();
                }
            });
        } else {
            send.run();
        }
    }

    /** Calls the listener with every message sent on the channel, by any instance including this one. */
    public void subscribe(String channel, Consumer<String> listener) {
        if (!this.enabled()) return;
        connectionFactory.getObject().getConnection().subscribe((message, pattern) -> listener.accept(message.toString()),
                channel.getBytes(StandardCharsets.UTF_8));
    }

}
