package xyz.erupt.cloud.example.blog;

import jakarta.annotation.Resource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.transaction.annotation.Transactional;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.jpa.dao.EruptDao;

/**
 * A few records to look at, on an empty database only. Written as the JSON the admin form would submit.
 */
// the stereotype is spelled out: Component is also the showcase entity of this package
@org.springframework.stereotype.Component
public class BlogSeed implements CommandLineRunner {

    @Resource
    private EruptDao eruptDao;

    @Override
    @Transactional
    public void run(String... args) {
        if (eruptDao.lambdaQuery(Component.class).count() == 0) {
            save(Component.class, "{'name':'Sample A','code':'C-001','roles':['ADMIN','USER'],'langs':['JAVA','GO'],'attrs':{'env':'prod','tier':'1'}}");
            save(Component.class, "{'name':'Sample B','code':'C-002','roles':['GUEST'],'langs':['PYTHON'],'attrs':{}}");
        }
        if (eruptDao.lambdaQuery(Category.class).count() > 0) return;
        Category tech = save(Category.class, "{'name':'Tech','sort':1}");
        Category python = save(Category.class, "{'name':'Python','sort':2,'parent':{'id':%d}}".formatted(tech.getId()));
        Category go = save(Category.class, "{'name':'Go','sort':3,'parent':{'id':%d}}".formatted(tech.getId()));
        Category life = save(Category.class, "{'name':'Life','sort':4}");
        Category travel = save(Category.class, "{'name':'Travel','sort':5,'parent':{'id':%d}}".formatted(life.getId()));
        save(Article.class, ("{'title':'Hello Erupt','summary':'A first article','category':{'id':%d},'topic':{'id':%d},"
                + "'status':'published','tags':'intro,erupt','views':120,'price':9.9,'rating':4.5,'progress':100,'featured':true,"
                + "'color':'#1677ff','icon':'fa fa-book','publishDate':'2026-10-01','reviewAt':'2026-10-02 10:00:00',"
                + "'content':'<p>Hello</p>','notes':'# Hello','meta':'{\"a\":1}'}").formatted(python.getId(), python.getId()));
        save(Article.class, ("{'title':'Go Tips','category':{'id':%d},'topic':{'id':%d},'status':'review','tags':'go',"
                + "'views':30,'price':0,'rating':3,'progress':60,'featured':false}").formatted(go.getId(), go.getId()));
        save(Article.class, ("{'title':'Weekend Trip','category':{'id':%d},'topic':{'id':%d},'status':'draft',"
                + "'views':0,'rating':0,'progress':10,'featured':false}").formatted(travel.getId(), travel.getId()));
    }

    private <T> T save(Class<T> type, String json) {
        T entity = GsonFactory.getGson().fromJson(json, type);
        eruptDao.persist(entity);
        return entity;
    }

}
