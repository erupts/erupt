package xyz.erupt.core.service;

import com.google.gson.JsonObject;
import org.springframework.stereotype.Service;
import xyz.erupt.core.model.EruptModel;
import xyz.erupt.core.model.Page;
import xyz.erupt.core.model.TreeModel;

import java.io.Serializable;
import java.util.Collection;

/**
 * Created by liyuepeng on 10/10/18.
 */
@Service
public interface DataService {

    Object findDataById(EruptModel eruptModel, Serializable id);

    Page queryList(EruptModel eruptModel, String customerCondition, JsonObject searchCondition, Page page);

    Collection<TreeModel> queryTree(EruptModel eruptModel);

    void addData(EruptModel eruptModel, Object object);

    void editData(EruptModel eruptModel, Object object);

    void deleteData(EruptModel eruptModel, Serializable id);

    // TAB Service
    Collection findTabList(EruptModel eruptModel, String fieldName);

    Collection findTabListById(EruptModel eruptModel, String fieldName, Serializable id);

    Collection<TreeModel> findTabTree(EruptModel eruptModel, String fieldName);

    Collection findTabTreeById(EruptModel eruptModel, String fieldName, Serializable id);


    //Reference Service
    Collection getReferenceTable(EruptModel eruptModel, String fieldName);

    Collection<TreeModel> getReferenceTree(EruptModel eruptModel, String fieldName);

    Collection<TreeModel> getReferenceTreeByDepend(EruptModel eruptModel, String fieldName, Serializable dependValue);

}
