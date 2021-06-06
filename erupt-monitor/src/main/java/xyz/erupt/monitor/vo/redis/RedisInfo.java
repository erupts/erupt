package xyz.erupt.monitor.vo.redis;

import lombok.Getter;
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
public class RedisInfo {

    private String version; //版本

    private String port;

    private String day; //运行天数

    private String clientNum; //客户端数量

    private String totalMem; //总内存

    private String usedMem; //使用内存

    private Long keyNum; //key数量

    private boolean isCluster; //是否集群

    private boolean isAOF;

    private List<RedisCmdStat> redisCmdStat;

    public RedisInfo(RedisConnectionFactory redisConnectionFactory) {
        RedisConnection redisConnection = redisConnectionFactory.getConnection();
        Optional.ofNullable(redisConnection.info("commandstats")).ifPresent(commandStats -> {
            String cs = "cmdstat_";
            redisCmdStat = commandStats.stringPropertyNames().stream().filter(it -> it.startsWith(cs))
                    .map(it -> new RedisCmdStat(StringUtils.removeStart(it, cs),
                            StringUtils.substringBetween(commandStats.getProperty(it), "calls=", ",usec"))).collect(Collectors.toList());
        });
        Optional.ofNullable(redisConnection.info()).ifPresent(properties -> {
            this.setKeyNum(redisConnectionFactory.getConnection().serverCommands().dbSize());
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
            this.setPort(properties.getProperty("tcp_port"));
            String clusterEnabled = properties.getProperty("cluster_enabled");
            if (null != clusterEnabled) {
                this.setCluster(Integer.parseInt(clusterEnabled) > 0);
            }
            String aofEnabled = properties.getProperty("aof_enabled");
            if (null != aofEnabled) {
                this.setAOF(Integer.parseInt(aofEnabled) != 0);
            }
        });
    }
}
