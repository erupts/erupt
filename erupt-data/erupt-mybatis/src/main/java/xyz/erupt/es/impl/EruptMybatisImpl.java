package xyz.erupt.es.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.query.Column;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.service.IEruptDataService;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;
import xyz.erupt.es.annotation.EruptMybatisORM;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author YuePeng
 * date 2020-03-06.
 */
@Service
public class EruptMybatisImpl implements IEruptDataService {

    public static final String MYBATIS_PROCESS = "mybatis";

    static {
        DataProcessorManager.register(MYBATIS_PROCESS, EruptMybatisImpl.class);
    }

    private BaseMapper getMapper(EruptModel eruptModel) {
        EruptMybatisORM eruptMybatisORM = eruptModel.getClazz().getAnnotation(EruptMybatisORM.class);
        if (null == eruptMybatisORM) {
            throw new RuntimeException("@" + EruptMybatisORM.class.getSimpleName() + " not found");
        }
        return EruptSpringUtil.getBean(eruptMybatisORM.mapper());
    }

    @Override
    public Object findDataById(EruptModel eruptModel, Object id) {
        return this.getMapper(eruptModel).selectById((Serializable) id);
    }

    @SneakyThrows
    @Override
    public Page queryList(EruptModel eruptModel, Page page, EruptQuery eruptQuery) {
        IPage p = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>();
        p.setSize(page.getPageSize());
        p.setCurrent(page.getPageIndex());
        QueryWrapper wrapper = new QueryWrapper<>();
        this.addQueryCondition(eruptQuery, wrapper);
        this.getMapper(eruptModel).selectPage(p, wrapper);
        page.setTotal(p.getTotal());
        page.setSort(page.getSort());
        page.setList(p.getRecords());
        return page;
    }

    public void addQueryCondition(EruptQuery eruptQuery, QueryWrapper queryWrapper) {
        for (Condition condition : eruptQuery.getConditions()) {
            switch (condition.getExpression()) {
                case EQ:
                    queryWrapper.eq(condition.getKey(), condition.getValue());
                    break;
                case LIKE:
                    queryWrapper.like(condition.getKey(), condition.getValue());
                    break;
                case RANGE:
//                    List<?> list = (List<?>) condition.getValue();
                    break;
                case IN:
                    queryWrapper.in(condition.getKey(), condition.getValue());
                    break;
            }
        }
    }


    @Override
    public void addData(EruptModel eruptModel, Object object) {
        this.getMapper(eruptModel).insert(object);
    }

    @Override
    public void editData(EruptModel eruptModel, Object object) {
        this.getMapper(eruptModel).updateById(object);
    }

    @Override
    public void deleteData(EruptModel eruptModel, Object object) {
        this.getMapper(eruptModel).deleteById((Serializable) object);
    }

    @Override
    public Collection<Map<String, Object>> queryColumn(EruptModel eruptModel, List<Column> columns, EruptQuery eruptQuery) {
        QueryWrapper wrapper = new QueryWrapper<>();
//        wrapper.select(columns.stream().map(Column::getName).collect(Collectors.toList()).toArray());
        this.addQueryCondition(eruptQuery, wrapper);
        List list = this.getMapper(eruptModel).selectList(wrapper);
        return list;
    }

}
