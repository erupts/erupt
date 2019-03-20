package xyz.erupt.core.service;

import com.google.gson.JsonObject;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import xyz.erupt.core.model.EruptFieldModel;
import xyz.erupt.core.model.EruptModel;
import xyz.erupt.core.model.Page;
import xyz.erupt.core.model.TreeModel;

import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * Created by liyuepeng on 10/10/18.
 */
@Service
public interface DataService {

    Page queryList(EruptModel eruptModel, JsonObject condition, Page page);

    Collection queryTree(EruptModel eruptModel);

    Object findDataById(EruptModel eruptModel, Serializable id);

    Collection findTabList(EruptFieldModel eruptTabFieldModel);

    Collection findTabListById(EruptModel eruptModel, String tabFieldName, Serializable id);

    Collection<TreeModel> findTabTree(EruptFieldModel eruptTabFieldModel);

    Collection findTabTreeById(EruptModel eruptModel, String tabFieldName, Serializable id);

    Collection getReferenceList(EruptModel eruptModel, String fieldName);

    void addData(EruptModel eruptModel, Object object);

    void editData(EruptModel eruptModel, Object object);

    void deleteData(EruptModel eruptModel, Serializable id);

}
