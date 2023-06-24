package xyz.erupt.jpa.service;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.query.Column;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.service.IEruptDataService;
import xyz.erupt.core.util.ReflectUtil;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;
import xyz.erupt.jpa.dao.EruptJpaDao;
import xyz.erupt.jpa.dao.EruptJpaUtils;
import xyz.erupt.jpa.support.JpaSupport;

import javax.annotation.Resource;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.transaction.Transactional;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @author YuePeng
 * date 2019-03-06.
 */
@Service
public class EruptDataServiceDbImpl implements IEruptDataService {

    static {
        DataProcessorManager.register(EruptConst.DEFAULT_DATA_PROCESSOR, EruptDataServiceDbImpl.class);
    }

    @Resource
    private EruptJpaDao eruptJpaDao;

    @Resource
    private EntityManagerService entityManagerService;

    @Resource
    private JpaSupport jpaSupport;

    @Override
    public Object findDataById(EruptModel eruptModel, Object id) {
        return entityManagerService.getEntityManager(eruptModel.getClazz(), (em) -> em.find(eruptModel.getClazz(), id));
    }

    @Override
    public Page queryList(EruptModel eruptModel, Page page, EruptQuery query) {
        return eruptJpaDao.queryEruptList(eruptModel, page, query);
    }

    @Transactional
    @Override
    public void addData(EruptModel eruptModel, Object data) {
        try {
            this.loadSupport(data);
            this.jpaManyToOneConvert(eruptModel, data);
            eruptJpaDao.addEntity(eruptModel.getClazz(), data);
        } catch (Exception e) {
            handlerException(e, eruptModel);
        }
    }

    @Override
    public void batchAddData(EruptModel eruptModel, List<?> objectList) {
        try {
            for (Object data : objectList) {
                this.loadSupport(data);
                this.jpaManyToOneConvert(eruptModel, data);
            }
            entityManagerService.entityManagerTran(eruptModel.getClazz(), (em) -> {
                for (int i = 0; i < objectList.size(); i++) {
                    Object entity = objectList.get(i);
                    em.persist(entity);
                    if (i % 500 == 0) em.flush();
                }
            });
        } catch (Exception e) {
            handlerException(e, eruptModel);
        }
    }

    @Transactional
    @Override
    public void editData(EruptModel eruptModel, Object data) {
        try {
            this.loadSupport(data);
            eruptJpaDao.editEntity(eruptModel.getClazz(), data);
        } catch (Exception e) {
            handlerException(e, eruptModel);
        }
    }

    private void loadSupport(Object jpaEntity) {
        for (Field field : jpaEntity.getClass().getDeclaredFields()) {
            jpaSupport.referencedColumnNameSupport(jpaEntity, field);
        }
    }

    //优化异常提示类
    private void handlerException(Exception e, EruptModel eruptModel) {
        e.printStackTrace();
        if (e instanceof DataIntegrityViolationException) {
            if (e.getMessage().contains("ConstraintViolationException")) {
                throw new EruptWebApiRuntimeException(gcRepeatHint(eruptModel));
            } else if (e.getMessage().contains("DataException")) {
                throw new EruptWebApiRuntimeException(I18nTranslate.$translate("erupt.data.limit_length"));
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
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("erupt.data.delete_fail_may_be_associated_data"));
        } catch (Exception e) {
            throw new EruptWebApiRuntimeException(e.getMessage());
        }
    }

    //@ManyToOne数据处理
    private void jpaManyToOneConvert(EruptModel eruptModel, Object object) throws IllegalAccessException {
        for (EruptFieldModel fieldModel : eruptModel.getEruptFieldModels()) {
            if (fieldModel.getEruptField().edit().type() == EditType.TAB_TABLE_ADD) {
                Field field = ReflectUtil.findClassField(object.getClass(), fieldModel.getFieldName());
                field.setAccessible(true);
                Collection<?> collection = (Collection<?>) field.get(object);
                if (null != collection) {
                    for (Object o : collection) {
                        //强制删除主键
                        ReflectUtil.findClassField(o.getClass(),
                                EruptCoreService.getErupt(fieldModel.getFieldReturnName()).getErupt()
                                        .primaryKeyCol()).set(o, null);
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
        String repeatTxt = I18nTranslate.$translate("erupt.data.data_duplication");
        if (StringUtils.isNotBlank(str)) {
            return str.substring(0, str.length() - 1) + " " + repeatTxt;
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
     * @return 数据结果集
     */
    @Override
    public Collection<Map<String, Object>> queryColumn(EruptModel eruptModel, List<Column> columns, EruptQuery query) {
        StringBuilder hql = new StringBuilder();
        List<String> columnStrList = new ArrayList<>();
        columns.forEach(column -> columnStrList.add(EruptJpaUtils.completeHqlPath(eruptModel.getEruptName()
                , column.getName()) + " as " + column.getAlias()));
        hql.append("select new map(").append(String.join(", ", columnStrList))
                .append(") from ").append(eruptModel.getEruptName()).append(" as ").append(eruptModel.getEruptName());
        ReflectUtil.findClassAllFields(eruptModel.getClazz(), field -> {
            if (null != field.getAnnotation(ManyToOne.class) || null != field.getAnnotation(OneToOne.class)) {
                hql.append(" left outer join ").append(eruptModel.getEruptName()).append(".")
                        .append(field.getName()).append(" as ").append(field.getName());
            }
        });
        hql.append(" where 1 = 1 ");
        Optional.ofNullable(query.getConditions()).ifPresent(c -> c.forEach(it -> hql.append(EruptJpaUtils.AND).append(it.getKey()).append('=').append(it.getValue())));
        Optional.ofNullable(query.getConditionStrings()).ifPresent(c -> c.forEach(it -> hql.append(EruptJpaUtils.AND).append(it)));
        Arrays.stream(eruptModel.getErupt().filter()).map(Filter::value)
                .filter(StringUtils::isNotBlank).forEach(it -> hql.append(EruptJpaUtils.AND).append(it));
        if (StringUtils.isNotBlank(query.getOrderBy())) {
            hql.append(" order by ").append(query.getOrderBy());
        }
        return entityManagerService.getEntityManager(eruptModel.getClazz(), (em) -> em.createQuery(hql.toString()).getResultList());
    }

}
