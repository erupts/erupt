package xyz.erupt.cloud.node.lite;

import org.junit.jupiter.api.Test;
import xyz.erupt.cloud.node.lite.Models.Article;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    @Test
    @SuppressWarnings("unchecked")
    void visFilterAndOrder() {
        add(Map.of("title", "a", "status", "draft"));
        add(Map.of("title", "a?", "status", "draft"));
        Answer question = call("POST", "/erupt-api/data/table/Article", Map.of("pageIndex", 1, "pageSize", 10, "vis", "q"));
        assertEquals(List.of("a?"), ((List<Map<String, Object>>) question.map().get("list")).stream().map(r -> r.get("title")).toList());
        // a view without a code is addressed by its title's hash, as erupt-core does
        Answer reversed = call("POST", "/erupt-api/data/table/Article", Map.of("pageIndex", 1, "pageSize", 10,
                "vis", Integer.toString("Reversed".hashCode())));
        assertEquals(List.of("a?", "a"), ((List<Map<String, Object>>) reversed.map().get("list")).stream().map(r -> r.get("title")).toList());
    }
}
