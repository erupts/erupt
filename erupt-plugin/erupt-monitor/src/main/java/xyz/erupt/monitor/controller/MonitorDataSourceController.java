package xyz.erupt.monitor.controller;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.monitor.constant.MonitorConstant;
import xyz.erupt.monitor.vo.DataSourcePool;
import xyz.erupt.upms.annotation.EruptMenuAuth;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * Exposes HikariCP pool stats. Loaded only when HikariCP is on the classpath, so
 * MongoDB-only deployments are unaffected. The frontend hides the card when the
 * endpoint is absent or returns empty.
 *
 * @author YuePeng
 * date 2026/6/15
 */
@RestController
@ConditionalOnClass(HikariDataSource.class)
@RequestMapping(MonitorConstant.REST_MONITOR + "/diagnosis")
public class MonitorDataSourceController {

    private final ObjectProvider<DataSource> dataSources;

    public MonitorDataSourceController(ObjectProvider<DataSource> dataSources) {
        this.dataSources = dataSources;
    }

    @GetMapping("/datasource")
    @EruptMenuAuth(MonitorConstant.MENU_DIAGNOSIS)
    public List<DataSourcePool> datasource() {
        List<DataSourcePool> pools = new ArrayList<>();
        dataSources.forEach(ds -> {
            if (ds instanceof HikariDataSource hikari) {
                pools.add(new DataSourcePool(hikari.getPoolName(), hikari));
            }
        });
        return pools;
    }

}
