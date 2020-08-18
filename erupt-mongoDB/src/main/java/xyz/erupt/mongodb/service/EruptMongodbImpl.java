package xyz.erupt.mongodb.service;

import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import xyz.erupt.core.service.EruptDataService;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;
import xyz.erupt.core.view.TreeModel;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * @author liyuepeng
 * @date 2019-03-06.
 */
@Service
public class EruptMongodbImpl implements EruptDataService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Object findDataById(EruptModel eruptModel, Object id) {
        Query query = new Query(Criteria.where(eruptModel.getErupt().primaryKeyCol()).is(id));
        return mongoTemplate.findOne(query, eruptModel.getClazz());
    }

    @Override
    public Page queryList(EruptModel eruptModel, Page page, JsonObject searchCondition, String... customCondition) {
        Query query = new Query();
        query.limit(page.getPageSize());
        query.skip(page.getPageIndex() * page.getPageSize());
        List list = mongoTemplate.find(query, eruptModel.getClazz());
        page.setList(list);
        return page;
    }

    @Override
    public Collection<TreeModel> queryTree(EruptModel eruptModel, String... customCondition) {
        return null;
    }

    @Override
    public void addData(EruptModel eruptModel, Object object) throws Exception {
        mongoTemplate.insert(object);
    }

    @Override
    public void editData(EruptModel eruptModel, Object object) throws Exception {
        mongoTemplate.save(object);
    }

    @Override
    public void deleteData(EruptModel eruptModel, Object object) {
        mongoTemplate.remove(object);
    }

    @Override
    public Collection<TreeModel> findTabTree(EruptModel eruptModel, String fieldName) {
        return null;
    }

    @Override
    public Collection<TreeModel> getReferenceTree(EruptModel eruptModel, String fieldName, Serializable dependValue, String... conditionStr) {
        return null;
    }
}
