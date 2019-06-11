package xyz.erupt.core.service.data_impl;

import com.google.gson.JsonObject;
import org.springframework.stereotype.Service;
import xyz.erupt.core.model.EruptFieldModel;
import xyz.erupt.core.model.EruptModel;
import xyz.erupt.core.model.Page;
import xyz.erupt.core.model.TreeModel;
import xyz.erupt.core.service.DataService;

import java.io.Serializable;
import java.util.Collection;

/**
 * Created by liyuepeng on 2019-04-29.
 */
@Service
public class NonDataService implements DataService {

    @Override
    public Page queryList(EruptModel eruptModel, JsonObject condition, Page page) {
        return new Page(1,0,"");
    }

    @Override
    public Collection<TreeModel> queryTree(EruptModel eruptModel) {
        return null;
    }

    @Override
    public Object findDataById(EruptModel eruptModel, Serializable id) {
        return null;
    }

    @Override
    public Collection findTabList(EruptFieldModel eruptTabFieldModel) {
        return null;
    }

    @Override
    public Collection findTabListById(EruptModel eruptModel, String tabFieldName, Serializable id) {
        return null;
    }

    @Override
    public Collection<TreeModel> findTabTree(EruptFieldModel eruptTabFieldModel) {
        return null;
    }

    @Override
    public Collection findTabTreeById(EruptModel eruptModel, String tabFieldName, Serializable id) {
        return null;
    }

    @Override
    public Collection getReferenceList(EruptModel eruptModel, String fieldName) {
        return null;
    }

    @Override
    public Collection<TreeModel> getReferenceTree(EruptModel eruptModel, String fieldName) {
        return null;
    }

    @Override
    public Collection<TreeModel> getReferenceTreeByDepend(EruptModel eruptModel, String fieldName, Serializable dependValue) {
        return null;
    }

    @Override
    public void addData(EruptModel eruptModel, Object object) {

    }

    @Override
    public void editData(EruptModel eruptModel, Object object) {

    }

    @Override
    public void deleteData(EruptModel eruptModel, Serializable id) {

    }
}
