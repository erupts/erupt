package xyz.erupt.monitor.vo.redis;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author liyuepeng
 * @date 2021/1/29 14:51
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

    private List<RedisCmdStat> redisCmdStat = new ArrayList<>();

    public RedisInfo(RedisConnectionFactory redisConnectionFactory) {
        RedisConnection redisConnection = redisConnectionFactory.getConnection();
        Properties properties = redisConnection.info();
        Properties commandStats = redisConnection.info("commandstats");
        if (null != commandStats) {
            commandStats.stringPropertyNames().forEach(key -> {
                String cs = "cmdstat_";
                if (key.startsWith(cs)) {
                    redisCmdStat.add(new RedisCmdStat(StringUtils.removeStart(key, cs),
                            StringUtils.substringBetween(commandStats.getProperty(key), "calls=", ",usec")));
                }
            });
        }
        if (null != properties) {
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
            this.setCluster(Integer.parseInt(properties.getProperty("cluster_enabled")) > 0);
            this.setAOF(Integer.parseInt(properties.getProperty("aof_enabled")) != 0);
            this.setPort(properties.getProperty("tcp_port"));
        }

    }
}
