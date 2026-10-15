package xyz.erupt.cloud.node.lite;

import xyz.erupt.cloud.node.lite.Models.Article;

class MemoryNodeTest extends NodeTest {

    @Override
    Repository<Article> articles() {
        return new MemoryRepository<>();
    }
}
