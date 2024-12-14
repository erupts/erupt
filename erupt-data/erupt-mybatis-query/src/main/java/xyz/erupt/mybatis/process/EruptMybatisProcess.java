package xyz.erupt.mybatis.process;

import xyz.erupt.annotation.fun.PowerObject;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.query.Column;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.service.IEruptDataService;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author YuePeng
 * date 2020-03-06.
 */
public abstract class EruptMybatisProcess implements IEruptDataService {

    public static final String MYBATIS_QUERY_PROCESS = "mybatis-query";

    static {
        DataProcessorManager.register(MYBATIS_QUERY_PROCESS, EruptMybatisProcess.class);
    }

    @Override
    public PowerObject power() {
        PowerObject powerObject = new PowerObject();
        powerObject.setAdd(false);
        powerObject.setEdit(false);
        powerObject.setDelete(false);
        powerObject.setImportable(false);
        return powerObject;
    }

    @Override
    public abstract Page queryList(EruptModel eruptModel, Page page, EruptQuery eruptQuery);

    @Override
    public Object findDataById(EruptModel eruptModel, Object id) {
        return null;
    }

    @Override
    public Collection<Map<String, Object>> queryColumn(EruptModel eruptModel, List<Column> columns, EruptQuery eruptQuery) {
        return Collections.emptyList();
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
