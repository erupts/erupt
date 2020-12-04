package xyz.erupt.core.service;

import xyz.erupt.core.query.Column;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author liyuepeng
 * @date 10/10/18.
 */
public interface IEruptDataService {

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
     * @param eruptModel erupt核心对象
     * @param page       page 分页参数
     * @param query      查询对象
     * @return 页面对象
     */
    Page queryList(EruptModel eruptModel, Page page, EruptQuery eruptQuery);

    /**
     * 根据列查询相关数据
     *
     * @param eruptModel eruptModel
     * @param columns    列
     * @param query      查询对象
     */
    Collection<Map<String, Object>> queryColumn(EruptModel eruptModel, List<Column> columns, EruptQuery eruptQuery);


    /**
     * 添加数据
     *
     * @param eruptModel erupt核心对象
     * @param object
     */
    void addData(EruptModel eruptModel, Object object);

    /**
     * 修改数据
     *
     * @param eruptModel erupt核心对象
     * @param object     数据对象
     */
    void editData(EruptModel eruptModel, Object object);

    /**
     * 删除数据
     *
     * @param eruptModel erupt核心对象
     * @param object     数据对象
     */
    void deleteData(EruptModel eruptModel, Object object);

}
