package xyz.erupt.mybatis;

import tk.mybatis.mapper.mapperhelper.EntityHelper;
import tk.mybatis.mapper.mapperhelper.MapperHelper;
import tk.mybatis.mapper.weekend.Weekend;
import xyz.erupt.core.query.Column;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.service.IEruptDataService;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author liyuepeng
 * @date 2020/12/8 20:49
 */
public class EruptMybatisImpl implements IEruptDataService {
    @Override
    public Object findDataById(EruptModel eruptModel, Object id) {
        Weekend weekend = new Weekend(eruptModel.getClazz());
        MapperHelper mapperHelper = new MapperHelper();
        EntityHelper.getEntityTable(eruptModel.getClazz());
        mapperHelper.registerMapper(eruptModel.getClazz());
        mapperHelper.getMapperTemplateByMsId("");
//        weekend.weekendCriteria().byid
        return null;
    }

    @Override
    public Page queryList(EruptModel eruptModel, Page page, EruptQuery eruptQuery) {
        return null;
    }

    @Override
    public Collection<Map<String, Object>> queryColumn(EruptModel eruptModel, List<Column> columns, EruptQuery eruptQuery) {
        return null;
    }

    @Override
    public void addData(EruptModel eruptModel, Object object) {

    }

    @Override
    public void editData(EruptModel eruptModel, Object object) {

    }

    @Override
    public void deleteData(EruptModel eruptModel, Object object) {

    }
}
