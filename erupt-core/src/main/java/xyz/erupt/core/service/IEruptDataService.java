package xyz.erupt.core.service;

import xyz.erupt.annotation.config.Comment;
import xyz.erupt.core.query.Column;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author YuePeng
 * date 10/10/18.
 */
public interface IEruptDataService {

    @Comment("根据主键id获取数据")
    Object findDataById(EruptModel eruptModel, @Comment("主键值") Object id);

    @Comment("查询分页数据")
    Page queryList(EruptModel eruptModel, @Comment("分页对象") Page page, @Comment("条件") EruptQuery eruptQuery);

    @Comment("根据列查询相关数据")
    Collection<Map<String, Object>> queryColumn(EruptModel eruptModel, @Comment("列信息") List<Column> columns, @Comment("条件") EruptQuery eruptQuery);

    @Comment("添加数据")
    void addData(EruptModel eruptModel, @Comment("数据对象") Object object);

    @Comment("修改数据")
    void editData(EruptModel eruptModel, @Comment("数据对象") Object object);

    @Comment("删除数据")
    void deleteData(EruptModel eruptModel, @Comment("数据对象") Object object);

}
