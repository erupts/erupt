package xyz.erupt.bi.service;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.bi.model.BiDataSource;
import xyz.erupt.core.exception.EruptApiErrorTip;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.core.util.TypeUtil;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author YuePeng
 * date 2020-02-28
 */
@Service
public class BiDataSourceService implements DataProxy<BiDataSource>, DisposableBean {

    private final Map<String, NamedParameterJdbcTemplate> templateMap = new HashMap<>();

    @Resource
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @SneakyThrows
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
                    hikariDataSource.setReadOnly(true); //报表数据源只读
                    hikariDataSource.setDriverClassName(biDataSource.getDriver());
                    hikariDataSource.setJdbcUrl(biDataSource.getUrl());
                    hikariDataSource.setPassword(biDataSource.getPassword());
                    hikariDataSource.setUsername(biDataSource.getUserName());
                    if (StringUtils.isNotBlank(biDataSource.getPoolConfig())) {
                        Properties properties = new Properties();
                        properties.load(new ByteArrayInputStream(biDataSource.getPoolConfig().getBytes()));
                        properties.forEach((k, v) -> {
                            try {
                                Field field = HikariConfig.class.getDeclaredField(k.toString());
                                field.setAccessible(true);
                                field.set(hikariDataSource, TypeUtil.typeStrConvertObject(v, field.getType()));
                            } catch (IllegalAccessException | NoSuchFieldException ignored) {
                            }
                        });
                    }
                    NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(hikariDataSource);
                    templateMap.put(biDataSource.getCode(), namedParameterJdbcTemplate);
                    return namedParameterJdbcTemplate;
                }
            }
            return jdbcTemplate;
        }
    }

    @Override
    public void destroy() {
        for (NamedParameterJdbcTemplate jdbcTemplate : templateMap.values()) {
            DataSource dataSource = jdbcTemplate.getJdbcTemplate().getDataSource();
            if (dataSource instanceof HikariDataSource) {
                HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
                hikariDataSource.close();
            }
        }
    }

    @SneakyThrows
    @Override
    public void beforeAdd(BiDataSource biDataSource) {
        try {
            if (null == biDataSource.getId()) {
                biDataSource.setCode(Erupts.generateCode());
            }
            Class.forName(biDataSource.getDriver());
        } catch (ClassNotFoundException e) {
            throw new EruptApiErrorTip(biDataSource.getName() + " JDBC Driver class not found");
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
    public void addBehavior(BiDataSource biDataSource) {
        biDataSource.setCode(Erupts.generateCode());
    }

    @Override
    public void afterDelete(BiDataSource biDataSource) {
        afterUpdate(biDataSource);
    }

}
