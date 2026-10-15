package xyz.erupt.cloud.node.lite;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * JdbcRepository against real PostgreSQL and MySQL. Runs only when ERUPT_TEST_DATABASES is set (the databases
 * workflow starts both as service containers); connection settings come from the standard PG* variables and
 * MYSQL_HOST / MYSQL_PORT / MYSQL_USER / MYSQL_PASSWORD / MYSQL_DATABASE.
 */
@EnabledIfEnvironmentVariable(named = "ERUPT_TEST_DATABASES", matches = ".+")
class DatabaseTest {

    @Test
    void postgres() throws Exception {
        DataSource ds = dataSource("jdbc:postgresql://" + env("PGHOST", "127.0.0.1") + ":" + env("PGPORT", "5432") + "/" + env("PGDATABASE", "erupt"),
                env("PGUSER", "postgres"), env("PGPASSWORD", ""));
        JdbcRepositoryTest.execute(ds, "DROP TABLE IF EXISTS \"db_post\"", "CREATE TABLE \"db_post\" (\"id\" SERIAL PRIMARY KEY, "
                + "\"title\" VARCHAR(255), \"views\" INTEGER, \"live\" BOOLEAN, \"day\" DATE, \"at\" TIMESTAMP(3))");
        JdbcRepositoryTest.scenario(ds);
    }

    @Test
    void mysql() throws Exception {
        DataSource ds = dataSource("jdbc:mysql://" + env("MYSQL_HOST", "127.0.0.1") + ":" + env("MYSQL_PORT", "3306") + "/" + env("MYSQL_DATABASE", "erupt"),
                env("MYSQL_USER", "root"), env("MYSQL_PASSWORD", ""));
        JdbcRepositoryTest.execute(ds, "DROP TABLE IF EXISTS `db_post`", "CREATE TABLE `db_post` (`id` INT AUTO_INCREMENT PRIMARY KEY, "
                + "`title` VARCHAR(255), `views` INT, `live` BOOLEAN, `day` DATE, `at` DATETIME(3))");
        JdbcRepositoryTest.scenario(ds);
    }

    private static String env(String name, String fallback) {
        return Optional.ofNullable(System.getenv(name)).orElse(fallback);
    }

    /** A DataSource over DriverManager: enough for a test, use a pool in production. */
    private static DataSource dataSource(String url, String user, String password) {
        return new DataSource() {
            @Override
            public Connection getConnection() throws java.sql.SQLException {
                return DriverManager.getConnection(url, user, password);
            }

            @Override
            public Connection getConnection(String u, String p) throws java.sql.SQLException {
                return DriverManager.getConnection(url, u, p);
            }

            @Override
            public PrintWriter getLogWriter() {
                return null;
            }

            @Override
            public void setLogWriter(PrintWriter out) {
            }

            @Override
            public void setLoginTimeout(int seconds) {
            }

            @Override
            public int getLoginTimeout() {
                return 0;
            }

            @Override
            public Logger getParentLogger() {
                return Logger.getGlobal();
            }

            @Override
            public <T> T unwrap(Class<T> iface) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isWrapperFor(Class<?> iface) {
                return false;
            }
        };
    }
}
