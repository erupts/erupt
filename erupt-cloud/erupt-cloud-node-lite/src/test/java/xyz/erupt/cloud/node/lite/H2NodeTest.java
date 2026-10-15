package xyz.erupt.cloud.node.lite;

import xyz.erupt.cloud.node.lite.Models.Article;

import javax.sql.DataSource;

/** The node tests with Article in H2 through JdbcRepository. */
class H2NodeTest extends NodeTest {

    @Override
    Repository<Article> articles() {
        DataSource ds = JdbcRepositoryTest.h2();
        try {
            JdbcRepositoryTest.execute(ds, "CREATE TABLE \"article\" (\"id\" BIGINT AUTO_INCREMENT PRIMARY KEY, \"title\" VARCHAR(255), "
                    + "\"status\" VARCHAR(16), \"level\" VARCHAR(8), \"views\" INT, \"featured\" BOOLEAN, \"day\" DATE, \"at\" TIMESTAMP(3), "
                    + "\"stamp\" VARCHAR(16), \"category\" BIGINT, \"topic\" BIGINT)");
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return new JdbcRepository<>(ds, "article");
    }
}
