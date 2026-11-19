package xyz.erupt.cloud.example.blog;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.jpa.dao.EruptDao;

import java.util.List;

/**
 * Row operation handlers of {@link Article}; the node runs them, the server only forwards the call.
 */
public class ArticleOperations {

    @Component
    public static class Publish implements OperationHandler<Article, PublishForm> {

        @Resource
        private EruptDao eruptDao;

        @Override
        @Transactional
        public String exec(List<Article> rows, PublishForm form, String[] param) {
            for (Article article : rows) {
                article.setStatus("published");
                article.setPublishDate(form.getPublishDate());
                eruptDao.merge(article);
            }
            return null;
        }

    }

    @Component
    public static class Feature implements OperationHandler<Article, Void> {

        @Resource
        private EruptDao eruptDao;

        @Override
        @Transactional
        public String exec(List<Article> rows, Void form, String[] param) {
            Article article = rows.get(0);
            article.setFeatured(true);
            eruptDao.merge(article);
            return null;
        }

    }

}
