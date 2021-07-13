package xyz.erupt.bi.service;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.bi.model.BiDataSource;
import xyz.erupt.core.exception.EruptApiErrorTip;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author YuePeng
 * date 2020-02-28
 */
@Service
public class BiDataSourceService implements DataProxy<BiDataSource> {

    private final Map<String, NamedParameterJdbcTemplate> templateMap = new HashMap<>();

    @Resource
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public NamedParameterJdbcTemplate getJdbcTemplate(BiDataSource biDataSource) {
        if (null == biDataSource) {
            return namedParameterJdbcTemplate;
        } else {
            NamedParameterJdbcTemplate jdbcTemplate = templateMap.get(biDataSource.getCode());
            if (null == jdbcTemplate) {
                synchronized (this) {
                    jdbcTemplate = templateMap.get(biDataSource.getCode());
                    if (null != jdbcTemplate) {
                        return jdbcTemplate;
                    }
                    HikariDataSource hikariDataSource = new HikariDataSource();
                    //报表数据源只读
                    hikariDataSource.setReadOnly(true);
                    hikariDataSource.setDriverClassName(biDataSource.getDriver());
                    hikariDataSource.setJdbcUrl(biDataSource.getUrl());
                    hikariDataSource.setPassword(biDataSource.getPassword());
                    hikariDataSource.setUsername(biDataSource.getUserName());
                    jdbcTemplate = new NamedParameterJdbcTemplate(hikariDataSource);
                    templateMap.put(biDataSource.getCode(), jdbcTemplate);
                }
            }
            return jdbcTemplate;
        }
    }


    @Override
    public void beforeAdd(BiDataSource biDataSource) {
        try {
            Class.forName(biDataSource.getDriver());
        } catch (ClassNotFoundException e) {
            throw new EruptApiErrorTip("找不到驱动类，请检查JDBC驱动包是否在项目内");
        }
    }

    @Override
    public void beforeUpdate(BiDataSource biDataSource) {
        this.beforeAdd(biDataSource);
    }

    @Override
    public void afterUpdate(BiDataSource biDataSource) {
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = templateMap.remove(biDataSource.getCode());
        if (null != namedParameterJdbcTemplate) {
            HikariDataSource hikariDataSource = (HikariDataSource) namedParameterJdbcTemplate.getJdbcTemplate().getDataSource();
            if (hikariDataSource != null && !hikariDataSource.isClosed()) {
                hikariDataSource.close();
            }
        }
    }

    @Override
    public void afterDelete(BiDataSource biDataSource) {
        afterUpdate(biDataSource);
    }
}
