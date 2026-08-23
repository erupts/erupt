package xyz.erupt.monitor.vo;

import com.zaxxer.hikari.HikariConfigMXBean;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import lombok.Getter;
import lombok.Setter;

/**
 * HikariCP connection pool runtime stats. Connection pool exhaustion is one of
 * the most common production incidents — surface it directly.
 *
 * @author YuePeng
 * date 2026/6/15
 */
@Getter
@Setter
public class DataSourcePool {

    private String name;

    private String jdbcUrl;

    private int active; // in-use connections

    private int idle;

    private int total;

    private int waiting; // threads awaiting a connection

    private int max;

    private int min;

    public DataSourcePool(String beanName, HikariDataSource ds) {
        HikariPoolMXBean pool = ds.getHikariPoolMXBean();
        HikariConfigMXBean config = ds.getHikariConfigMXBean();
        this.name = ds.getPoolName() == null ? beanName : ds.getPoolName();
        this.jdbcUrl = ds.getJdbcUrl();
        if (pool != null) {
            this.active = pool.getActiveConnections();
            this.idle = pool.getIdleConnections();
            this.total = pool.getTotalConnections();
            this.waiting = pool.getThreadsAwaitingConnection();
        }
        this.max = config.getMaximumPoolSize();
        this.min = config.getMinimumIdle();
    }

}
