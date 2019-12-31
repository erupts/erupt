package xyz.erupt.core.service;

import com.google.gson.JsonObject;
import xyz.erupt.core.bean.EruptModel;
import xyz.erupt.core.bean.Page;
import xyz.erupt.core.bean.TreeModel;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author liyuepeng
 * @date 10/10/18.
 */
public interface DataService {

    /**
     * 根据主键id获取数据
     *
     * @param eruptModel erupt核心对象
     * @param id         id
     * @return 通过id获取到的数据
     */
    Object findDataById(EruptModel eruptModel, Object id);

    /**
     * 查询分页数据
     *
     * @param eruptModel        erupt核心对象
     * @param page              page 分页参数
     * @param searchCondition   参数查询条件
     * @param customerCondition 自定义查询条件
     * @return 页面对象
     */
    Page queryList(EruptModel eruptModel, Page page, JsonObject searchCondition, String customerCondition);

    /**
     * 查询树形结构数据
     *
     * @param eruptModel erupt核心对象
     * @return list tree数据
     */
    Collection<TreeModel> queryTree(EruptModel eruptModel);

    /**
     * 添加数据
     *
     * @param eruptModel erupt核心对象
     * @param object
     */
    void addData(EruptModel eruptModel, Object object) throws Exception;

    /**
     * 修改数据
     *
     * @param eruptModel erupt核心对象
     * @param object     数据对象
     */
    void editData(EruptModel eruptModel, Object object) throws Exception;

    /**
     * 删除数据
     *
     * @param eruptModel erupt核心对象
     * @param id         数据id
     */
    void deleteData(EruptModel eruptModel, Object id) throws Exception;

    /**
     * 获取tab栏参照的树
     *
     * @param eruptModel erupt核心对象
     * @param fieldName  字段名称
     * @return
     */
    Collection<TreeModel> findTabTree(EruptModel eruptModel, String fieldName);

    /**
     * 获取参照的树
     *
     * @param eruptModel  erupt核心对象
     * @param fieldName   字段名称
     * @param dependValue 参照值
     * @return
     */
    Collection<TreeModel> getReferenceTree(EruptModel eruptModel, String fieldName, Serializable dependValue);

}
