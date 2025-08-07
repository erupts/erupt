package xyz.erupt.jpa.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.query.Column;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.service.IEruptDataService;
import xyz.erupt.core.util.ReflectUtil;
import xyz.erupt.core.util.TypeUtil;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;
import xyz.erupt.jpa.dao.EruptJpaDao;
import xyz.erupt.jpa.dao.EruptJpaUtils;
import xyz.erupt.jpa.support.JpaSupport;

import javax.annotation.Resource;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @author YuePeng
 * date 2019-03-06.
 */
@Slf4j
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

    @Override
    @SneakyThrows
    public void addData(EruptModel eruptModel, Object data) {
        this.loadSupport(data);
        this.jpaManyToOneConvert(eruptModel, data);
        eruptJpaDao.addEntity(eruptModel.getClazz(), data);
    }

    @Override
    public void editData(EruptModel eruptModel, Object data) {
        this.loadSupport(data);
        eruptJpaDao.editEntity(eruptModel.getClazz(), data);
    }

    @Override
    @SneakyThrows
    public void batchAddData(EruptModel eruptModel, List<?> objectList) {
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
    }

    private void loadSupport(Object jpaEntity) {
        for (Field field : jpaEntity.getClass().getDeclaredFields()) {
            jpaSupport.referencedColumnNameSupport(jpaEntity, field);
        }
    }

    @Override
    public void deleteData(EruptModel eruptModel, Object object) {
        eruptJpaDao.removeEntity(eruptModel.getClazz(), object);
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
        Set<String> aliasSet = new HashSet<>();
        ReflectUtil.findClassAllFields(eruptModel.getClazz(), field -> {
            if (null != field.getAnnotation(ManyToOne.class) || null != field.getAnnotation(OneToOne.class)) {
                if (!aliasSet.contains(field.getName())) {
                    hql.append(" left outer join ").append(eruptModel.getEruptName()).append(EruptConst.DOT)
                            .append(field.getName()).append(" as ").append(field.getName());
                    aliasSet.add(field.getName());
                }
            }
        });
        hql.append(" where 1 = 1 ");
        Optional.ofNullable(query.getConditions()).ifPresent(c -> c.forEach(it -> {
            hql.append(EruptJpaUtils.AND).append(it.getKey()).append('=');
            if (TypeUtil.isNumber(it.getValue())) {
                hql.append(it.getValue());
            } else {
                hql.append("'").append(it.getValue()).append("'");
            }
        }));
        Optional.ofNullable(query.getConditionStrings()).ifPresent(c -> c.forEach(it -> hql.append(EruptJpaUtils.AND).append(it)));
        Arrays.stream(eruptModel.getErupt().filter()).map(Filter::value)
                .filter(StringUtils::isNotBlank).forEach(it -> hql.append(EruptJpaUtils.AND).append(it));
        if (StringUtils.isNotBlank(query.getOrderBy())) {
            hql.append(" order by ").append(query.getOrderBy());
        }
        return entityManagerService.getEntityManager(eruptModel.getClazz(), (em) -> em.createQuery(hql.toString()).getResultList());
    }

}
