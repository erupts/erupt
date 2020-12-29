package xyz.erupt.jpa.service;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.query.Column;
import xyz.erupt.core.query.Condition;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.service.IEruptDataService;
import xyz.erupt.core.util.AnnotationUtil;
import xyz.erupt.core.util.DataProcessorManager;
import xyz.erupt.core.util.ReflectUtil;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;
import xyz.erupt.jpa.dao.EruptJpaDao;
import xyz.erupt.jpa.dao.EruptJpaUtils;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author liyuepeng
 * @date 2019-03-06.
 */
@Service
public class EruptDataServiceDbImpl implements IEruptDataService, ApplicationRunner {

    @Autowired
    private EruptJpaDao eruptJpaDao;

    @Autowired
    private EntityManagerService entityManagerService;

    @Override
    public Object findDataById(EruptModel eruptModel, Object id) {
        EntityManager entityManager = entityManagerService.getEntityManager(eruptModel.getClazz());
        Object obj = entityManager.find(eruptModel.getClazz(), id);
        if (entityManager.isOpen()) {
            entityManager.close();
        }
        return obj;
    }

    @Override
    public Page queryList(EruptModel eruptModel, Page page, EruptQuery query) {
        return eruptJpaDao.queryEruptList(eruptModel, page, query);
    }

    @Transactional
    @Override
    public void addData(EruptModel eruptModel, Object object) {
        try {
            jpaManyToOneConvert(eruptModel, object);
            eruptJpaDao.addEntity(eruptModel.getClazz(), object);
        } catch (Exception e) {
            handlerException(e, eruptModel);
        }
    }

    @Transactional
    @Override
    public void editData(EruptModel eruptModel, Object data) {
        try {
            eruptJpaDao.editEntity(eruptModel.getClazz(), data);
        } catch (Exception e) {
            handlerException(e, eruptModel);
        }
    }

    //优化异常提示类
    private void handlerException(Exception e, EruptModel eruptModel) {
        if (e instanceof DataIntegrityViolationException) {
            if (e.getMessage().contains("ConstraintViolationException")) {
                throw new EruptWebApiRuntimeException(gcRepeatHint(eruptModel));
            } else if (e.getMessage().contains("DataException")) {
                throw new EruptWebApiRuntimeException("内容超出数据库限制长度！");
            } else {
                throw new EruptWebApiRuntimeException(e.getMessage());
            }
        } else {
            throw new EruptWebApiRuntimeException(e.getMessage());
        }
    }

    @Transactional
    @Override
    public void deleteData(EruptModel eruptModel, Object object) {
        try {
            eruptJpaDao.removeEntity(eruptModel.getClazz(), object);
        } catch (DataIntegrityViolationException | ConstraintViolationException e) {
            e.printStackTrace();
            throw new EruptWebApiRuntimeException("删除失败，可能存在关联数据，无法直接删除！");
        } catch (Exception e) {
            throw new EruptWebApiRuntimeException(e.getMessage());
        }
    }

    //@ManyToOne数据处理
    private void jpaManyToOneConvert(EruptModel eruptModel, Object object) throws NoSuchFieldException, IllegalAccessException {
        for (EruptFieldModel fieldModel : eruptModel.getEruptFieldModels()) {
            if (fieldModel.getEruptField().edit().type() == EditType.TAB_TABLE_ADD) {
                Field field = object.getClass().getDeclaredField(fieldModel.getFieldName());
                field.setAccessible(true);
                Collection collection = (Collection) field.get(object);
                if (null != collection) {
                    for (Object o : collection) {
                        //删除主键ID
                        //TODO 强制删除id的处理方式并不好
                        Field pk = ReflectUtil.findClassField(o.getClass(), EruptCoreService
                                .getErupt(fieldModel.getFieldReturnName()).getErupt().primaryKeyCol());
                        pk.set(o, null);
                    }
                }
            }
        }
    }

    //生成数据重复的提示字符串
    private String gcRepeatHint(EruptModel eruptModel) {
        StringBuilder str = new StringBuilder();
        for (UniqueConstraint uniqueConstraint : eruptModel.getClazz().getAnnotation(Table.class).uniqueConstraints()) {
            for (String columnName : uniqueConstraint.columnNames()) {
                EruptFieldModel eruptFieldModel = eruptModel.getEruptFieldMap().get(columnName);
                if (null != eruptFieldModel) {
                    str.append(eruptFieldModel.getEruptField().views()[0].title()).append("、");
                }
            }
        }
        String repeatTxt = "数据重复";
        if (StringUtils.isNotBlank(str)) {
            return str.substring(0, str.length() - 1) + repeatTxt;
        } else {
            return repeatTxt;
        }
    }

    /**
     * 根据列获取相关数据
     *
     * @param eruptModel eruptModel
     * @param columns    列
     * @param query      查询对象
     * @return
     */
    @Override
    public Collection<Map<String, Object>> queryColumn(EruptModel eruptModel, List<Column> columns, EruptQuery query) {
        StringBuilder hql = new StringBuilder();
        List<String> columnStrList = new ArrayList<>();
        for (Column column : columns) {
            columnStrList.add(EruptJpaUtils.completeHqlPath(eruptModel.getEruptName(), column.getName()) + " as " + column.getAlias());
        }
        hql.append("select new map(").append(String.join(", ", columnStrList))
                .append(") from ").append(eruptModel.getEruptName()).append(" as ").append(eruptModel.getEruptName());
        ReflectUtil.findClassAllFields(eruptModel.getClazz(), field -> {
            if (null != field.getAnnotation(ManyToOne.class) || null != field.getAnnotation(OneToOne.class)) {
                hql.append(" left outer join ").append(eruptModel.getEruptName()).append(".")
                        .append(field.getName()).append(" as ").append(field.getName());
            }
        });
        hql.append(" where 1 = 1 ");
        if (null != query.getConditions()) {
            for (Condition condition : query.getConditions()) {
                hql.append(EruptJpaUtils.AND).append(condition.getKey()).append('=').append(condition.getValue());
            }
        }
        if (null != query.getConditionStrings()) {
            for (String condition : query.getConditionStrings()) {
                hql.append(EruptJpaUtils.AND).append(condition);
            }
        }
        for (Filter filter : eruptModel.getErupt().filter()) {
            String filterStr = AnnotationUtil.switchFilterConditionToStr(filter);
            if (StringUtils.isNotBlank(filterStr)) {
                hql.append(EruptJpaUtils.AND).append(filterStr);
            }
        }
        if (StringUtils.isNotBlank(query.getOrderBy())) {
            hql.append(" order by ").append(query.getOrderBy());
        }
        EntityManager entityManager = entityManagerService.getEntityManager(eruptModel.getClazz());
        List list = entityManager.createQuery(hql.toString()).getResultList();
        if (entityManager.isOpen()) {
            entityManager.close();
        }
        return list;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        DataProcessorManager.register(EruptConst.DEFAULT_DATA_PROCESSOR, EruptDataServiceDbImpl.class);
    }
}
