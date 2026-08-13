package xyz.erupt.jpa.config;

import com.zaxxer.hikari.HikariConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * @author YuePeng
 * date 2021/9/25 23:28
 */
@Getter
@Setter
public class HikariCpConfig {

    private static final long CONNECTION_TIMEOUT = TimeUnit.SECONDS.toMillis(30L);
    private static final long VALIDATION_TIMEOUT = TimeUnit.SECONDS.toMillis(5L);
    private static final long IDLE_TIMEOUT = TimeUnit.MINUTES.toMillis(10L);
    private static final long MAX_LIFETIME = TimeUnit.MINUTES.toMillis(30L);
    private static final int DEFAULT_POOL_SIZE = 10;

    private String username;

    private String password;

    private String driverClassName;

    private String jdbcUrl;

    private String poolName;

    private String catalog;

    private Long connectionTimeout;

    private Long validationTimeout;

    private Long idleTimeout;

    private Long leakDetectionThreshold;

    private Long maxLifetime;

    private Integer maxPoolSize;

    private Integer minIdle;

    private Long initializationFailTimeout;

    private String connectionInitSql;

    private String connectionTestQuery;

    private String dataSourceClassName;

    private String dataSourceJndiName;

    private String schema;

    private String transactionIsolationName;

    private Boolean isAutoCommit;

    private Boolean isReadOnly;

    private Boolean isIsolateInternalQueries;

    private Boolean isRegisterMbeans;

    private Boolean isAllowPoolSuspension;

    private Properties dataSourceProperties;

    private Properties healthCheckProperties;

    @SneakyThrows
    public HikariConfig toHikariConfig() {
        HikariCpConfig g = this;
        HikariConfig config = new HikariConfig();
        String tempSchema = this.schema == null ? g.getSchema() : this.schema;
        if (tempSchema != null) {
            Field schemaField = HikariConfig.class.getDeclaredField("schema");
            schemaField.setAccessible(true);
            schemaField.set(config, tempSchema);
        }
        String tempCatalog = this.catalog == null ? g.getCatalog() : this.catalog;
        if (tempCatalog != null) {
            config.setCatalog(tempCatalog);
        }

        Long tempConnectionTimeout = this.connectionTimeout == null ? g.getConnectionTimeout() : this.connectionTimeout;
        if (tempConnectionTimeout != null && !tempConnectionTimeout.equals(CONNECTION_TIMEOUT)) {
            config.setConnectionTimeout(tempConnectionTimeout);
        }

        Long tempValidationTimeout = this.validationTimeout == null ? g.getValidationTimeout() : this.validationTimeout;
        if (tempValidationTimeout != null && !tempValidationTimeout.equals(VALIDATION_TIMEOUT)) {
            config.setValidationTimeout(tempValidationTimeout);
        }

        Long tempIdleTimeout = this.idleTimeout == null ? g.getIdleTimeout() : this.idleTimeout;
        if (tempIdleTimeout != null && !tempIdleTimeout.equals(IDLE_TIMEOUT)) {
            config.setIdleTimeout(tempIdleTimeout);
        }

        Long tempLeakDetectionThreshold = this.leakDetectionThreshold == null ? g.getLeakDetectionThreshold() : this.leakDetectionThreshold;
        if (tempLeakDetectionThreshold != null) {
            config.setLeakDetectionThreshold(tempLeakDetectionThreshold);
        }

        Long tempMaxLifetime = this.maxLifetime == null ? g.getMaxLifetime() : this.maxLifetime;
        if (tempMaxLifetime != null && !tempMaxLifetime.equals(MAX_LIFETIME)) {
            config.setMaxLifetime(tempMaxLifetime);
        }

        Integer tempMaxPoolSize = this.maxPoolSize == null ? g.getMaxPoolSize() : this.maxPoolSize;
        if (tempMaxPoolSize != null && !tempMaxPoolSize.equals(-1)) {
            config.setMaximumPoolSize(tempMaxPoolSize);
        }

        Integer tempMinIdle = this.minIdle == null ? g.getMinIdle() : this.getMinIdle();
        if (tempMinIdle != null && !tempMinIdle.equals(-1)) {
            config.setMinimumIdle(tempMinIdle);
        }

        Long tempInitializationFailTimeout = this.initializationFailTimeout == null ? g.getInitializationFailTimeout() : this.initializationFailTimeout;
        if (tempInitializationFailTimeout != null && !tempInitializationFailTimeout.equals(1L)) {
            config.setInitializationFailTimeout(tempInitializationFailTimeout);
        }

        String tempConnectionInitSql = this.connectionInitSql == null ? g.getConnectionInitSql() : this.connectionInitSql;
        if (tempConnectionInitSql != null) {
            config.setConnectionInitSql(tempConnectionInitSql);
        }

        String tempConnectionTestQuery = this.connectionTestQuery == null ? g.getConnectionTestQuery() : this.connectionTestQuery;
        if (tempConnectionTestQuery != null) {
            config.setConnectionTestQuery(tempConnectionTestQuery);
        }

        String tempDataSourceClassName = this.dataSourceClassName == null ? g.getDataSourceClassName() : this.dataSourceClassName;
        if (tempDataSourceClassName != null) {
            config.setDataSourceClassName(tempDataSourceClassName);
        }

        String tempDataSourceJndiName = this.dataSourceJndiName == null ? g.getDataSourceJndiName() : this.dataSourceJndiName;
        if (tempDataSourceJndiName != null) {
            config.setDataSourceJNDI(tempDataSourceJndiName);
        }

        String tempTransactionIsolationName = this.transactionIsolationName == null ? g.getTransactionIsolationName() : this.transactionIsolationName;
        if (tempTransactionIsolationName != null) {
            config.setTransactionIsolation(tempTransactionIsolationName);
        }

        Boolean tempAutoCommit = this.isAutoCommit == null ? g.getIsAutoCommit() : this.isAutoCommit;
        if (tempAutoCommit != null && tempAutoCommit.equals(Boolean.FALSE)) {
            config.setAutoCommit(false);
        }

        Boolean tempReadOnly = this.isReadOnly == null ? g.getIsReadOnly() : this.isReadOnly;
        if (tempReadOnly != null) {
            config.setReadOnly(tempReadOnly);
        }

        Boolean tempIsolateInternalQueries = this.isIsolateInternalQueries == null ? g.getIsIsolateInternalQueries() : this.isIsolateInternalQueries;
        if (tempIsolateInternalQueries != null) {
            config.setIsolateInternalQueries(tempIsolateInternalQueries);
        }

        Boolean tempRegisterMbeans = this.isRegisterMbeans == null ? g.getIsRegisterMbeans() : this.isRegisterMbeans;
        if (tempRegisterMbeans != null) {
            config.setRegisterMbeans(tempRegisterMbeans);
        }

        Boolean tempAllowPoolSuspension = this.isAllowPoolSuspension == null ? g.getIsAllowPoolSuspension() : this.isAllowPoolSuspension;
        if (tempAllowPoolSuspension != null) {
            config.setAllowPoolSuspension(tempAllowPoolSuspension);
        }

        Properties tempDataSourceProperties = this.dataSourceProperties == null ? g.getDataSourceProperties() : this.dataSourceProperties;
        if (tempDataSourceProperties != null) {
            config.setDataSourceProperties(tempDataSourceProperties);
        }

        Properties tempHealthCheckProperties = this.healthCheckProperties == null ? g.getHealthCheckProperties() : this.healthCheckProperties;
        if (tempHealthCheckProperties != null) {
            config.setHealthCheckProperties(tempHealthCheckProperties);
        }

        return config;
    }
}

