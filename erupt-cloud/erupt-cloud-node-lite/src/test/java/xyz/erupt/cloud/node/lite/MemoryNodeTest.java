package xyz.erupt.cloud.node.lite;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

import xyz.erupt.cloud.node.lite.Models.Article;

class MemoryNodeTest extends NodeTest {

    @Override
    Repository<Article> articles() {
        return new MemoryRepository<>();
    }

    @Test
    void beforeFetchSqlIsRefused() {
        MemoryRepository<Article> repo = new MemoryRepository<>();
        repo.bind(Model.of(Article.class));
        assertThrows(IllegalStateException.class, () -> repo.page(new Query(List.of(), List.of(), 0, 10, List.of("1=1"))));
    }
}
