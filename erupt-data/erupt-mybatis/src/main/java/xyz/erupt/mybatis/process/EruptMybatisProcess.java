package xyz.erupt.mybatis.process;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.SneakyThrows;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.query.Column;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.service.IEruptDataService;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author YuePeng
 * date 2020-03-06.
 */
@Service
public class EruptMybatisProcess implements IEruptDataService {

    public static final String MYBATIS_PROCESS = "mybatis";

    @Resource
    private SqlSessionFactory sqlSessionFactory;

    @Resource
    private SqlSessionTemplate sqlSessionTemplate;

    static {
        DataProcessorManager.register(MYBATIS_PROCESS, EruptMybatisProcess.class);
    }

    @SneakyThrows
    private BaseMapper<Object> getMapper(EruptModel eruptModel) {
//        return sqlSessionTemplate.getMapper(eruptModel.getClazz());
        return (BaseMapper<Object>) sqlSessionFactory.openSession().getMapper(eruptModel.getClazz());
    }

    @Override
    public Object findDataById(EruptModel eruptModel, Object id) {
        return this.getMapper(eruptModel).selectById((Serializable) id);
    }

    @SneakyThrows
    @Override
    public Page queryList(EruptModel eruptModel, Page page, EruptQuery eruptQuery) {
        IPage<Object> p = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>();
        p.setSize(page.getPageSize());
        p.setCurrent(page.getPageIndex());
        QueryWrapper<Object> wrapper = new QueryWrapper<>();
        this.addQueryCondition(eruptQuery, wrapper);
        this.getMapper(eruptModel).selectPage(p, wrapper);
        page.setTotal(p.getTotal());
        page.setSort(page.getSort());
        page.setList(null);
        return page;
    }

    public void addQueryCondition(EruptQuery eruptQuery, QueryWrapper<?> queryWrapper) {
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
        this.getMapper(eruptModel).deleteById(object.toString());
    }

    @Override
    public Collection<Map<String, Object>> queryColumn(EruptModel eruptModel, List<Column> columns, EruptQuery eruptQuery) {
        QueryWrapper<Object> queryWrapper = Wrappers.query(eruptModel.getClazz());
        List<String> list = new ArrayList<>();
        for (Column column : columns) {
            list.add(column.getName());
        }
        queryWrapper.select(list.toArray(new String[]{}));

        this.getMapper(eruptModel).selectList(queryWrapper);
        return null;
    }

}
