package xyz.erupt.monitor.vo.redis;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author YuePeng
 * date 2021/1/29 14:51
 */
@Getter
@Setter
@NoArgsConstructor
public class RedisInfo {

    private String version;

    private String port;

    private String day;

    private String clientNum;

    private String totalMem;

    private String usedMem;

    private String usedMemPeak;

    private String fragRatio;

    private Long keyNum;

    private Long evictedKeys;

    private String ops;

    private String hitRate;

    private boolean isCluster;

    private boolean isAOF;

    private String rdbStatus;

    private List<RedisCmdStat> redisCmdStat;

    public RedisInfo(RedisConnectionFactory redisConnectionFactory) {
        RedisConnection redisConnection = redisConnectionFactory.getConnection();
        Optional.of(redisConnection.info("commandstats")).ifPresent(commandStats -> {
            String cs = "cmdstat_";
            redisCmdStat = commandStats.stringPropertyNames().stream().filter(it -> it.startsWith(cs))
                    .map(it -> new RedisCmdStat(StringUtils.removeStart(it, cs),
                            StringUtils.substringBetween(commandStats.getProperty(it), "calls=", ",usec"))).collect(Collectors.toList());
        });
        Optional.of(redisConnection.info()).ifPresent(properties -> {
            this.setKeyNum(redisConnection.serverCommands().dbSize());
            this.setVersion(properties.getProperty("redis_version"));
            this.setUsedMem(properties.getProperty("used_memory_human"));
            if ("0".equals(properties.getProperty("maxmemory"))) {
                this.setTotalMem(properties.getProperty("total_system_memory_human"));
            } else {
                this.setTotalMem(properties.getProperty("maxmemory_human"));
            }
            this.setPort(properties.getProperty("tcp_port"));
            this.setDay(properties.getProperty("uptime_in_days"));
            this.setClientNum(properties.getProperty("connected_clients"));
            this.setUsedMemPeak(properties.getProperty("used_memory_peak_human"));
            this.setFragRatio(properties.getProperty("mem_fragmentation_ratio"));
            this.setOps(properties.getProperty("instantaneous_ops_per_sec"));
            String hits = properties.getProperty("keyspace_hits");
            String misses = properties.getProperty("keyspace_misses");
            if (hits != null && misses != null) {
                long h = Long.parseLong(hits);
                long m = Long.parseLong(misses);
                long total = h + m;
                this.setHitRate(total > 0 ? String.format("%.1f", (double) h / total * 100) : null);
            }
            String evicted = properties.getProperty("evicted_keys");
            if (evicted != null) this.setEvictedKeys(Long.parseLong(evicted));
            this.setRdbStatus(properties.getProperty("rdb_last_bgsave_status"));
            String clusterEnabled = properties.getProperty("cluster_enabled");
            if (null != clusterEnabled) {
                this.setCluster(Integer.parseInt(clusterEnabled) > 0);
            }
            String aofEnabled = properties.getProperty("aof_enabled");
            if (null != aofEnabled) {
                this.setAOF(Integer.parseInt(aofEnabled) != 0);
            }
        });
        redisConnection.close();
    }
}
