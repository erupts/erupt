package xyz.erupt.jpa.dao;

import org.apache.commons.lang3.StringUtils;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.util.ReflectUtil;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author YuePeng date 2018-11-05.
 */
public class EruptJpaUtils {

    public static final String L_VAL_KEY = "l_";

    public static final String R_VAL_KEY = "r_";

    public static final String PERCENT = "%";

    public static final String AND = " and ";

    public static final String AS = " as ";

    public static final String LEFT_JOIN = " left outer join ";

    public static Set<String> getEruptColJpaKeys(EruptModel eruptModel) {
        Set<String> cols = new HashSet<>();
        String eruptNameSymbol = eruptModel.getEruptName() + EruptConst.DOT;
        cols.add(eruptNameSymbol + eruptModel.getErupt().primaryKeyCol() + AS + eruptModel.getErupt().primaryKeyCol());
        eruptModel.getEruptFieldModels().forEach(field -> {
            if (null != field.getField().getAnnotation(OneToMany.class) || null != field.getField().getAnnotation(ManyToMany.class) || null != field.getField().getAnnotation(ElementCollection.class)) {
                return;
            }
            if (null == field.getField().getAnnotation(OneToOne.class) && field.getEruptField().edit().type() == EditType.COMBINE) {
                return;
            }
            if (null != field.getField().getAnnotation(Transient.class)) {
                return;
            }
            for (View view : field.getEruptField().views()) {
                if (view.column().isEmpty()) {
                    cols.add(eruptNameSymbol + field.getFieldName() + AS + field.getFieldName());
                } else {
                    cols.add(eruptNameSymbol + field.getFieldName() + EruptConst.DOT + view.column() + AS + field.getFieldName() + "_"
                            + view.column().replace(EruptConst.DOT, "_"));
                }
            }
        });
        return cols;
    }

    //erupt 注解信息映射成hql语句
    public static String generateEruptJpaHql(EruptModel eruptModel, String cols, EruptQuery query, boolean countSql) {
        StringBuilder hql = new StringBuilder();
        if (StringUtils.isNotBlank(cols)) {
            hql.append("select ").append(cols).append(" from ")
                    .append(eruptModel.getEruptName()).append(AS).append(eruptModel.getEruptName());
            // 修复view配置多级显示时查询结果不正确的缺陷
            // 如果view配置了多级显示，则必须手动进行left join 关联，否则会因jpa自动生成的cross join 导致查询结果不完整。
            // 在这里调用改写的 generateEruptJoinHql 方法
            hql.append(generateEruptJoinHql(eruptModel));

        } else {
            hql.append("from ").append(eruptModel.getEruptName());
        }
        hql.append(geneEruptHqlCondition(eruptModel, query.getConditions(), query.getConditionStrings()));
        if (!countSql) {
            hql.append(geneEruptHqlOrderBy(eruptModel, query.getOrderBy()));
        }
        return hql.toString();
    }

    public static String generateEruptJoinHql(EruptModel eruptModel) {
        StringBuffer hql = new StringBuffer();
        Set<String> pathSet = new HashSet<>();
        Set<String> aliasSet = new HashSet<>();
        ReflectUtil.findClassAllFields(eruptModel.getClazz(), field -> {
            if (null != field.getAnnotation(ManyToOne.class) || null != field.getAnnotation(OneToOne.class)) {
                EruptFieldModel model = eruptModel.getEruptFieldMap().get(field.getName());
                if (model != null) {
                    View[] views = model.getEruptField().views();
                    for (View v : views) {
                        String columnPath = v.column();
                        if (columnPath.contains(EruptConst.DOT)) {
                            String path = eruptModel.getEruptName() + EruptConst.DOT + field.getName() + EruptConst.DOT + columnPath.substring(0, columnPath.lastIndexOf(EruptConst.DOT));
                            if (!pathSet.contains(path)) {
                                hql.append(LEFT_JOIN).append(path);
                                pathSet.add(path);
                            }
                        } else {
                            String alias = field.getName();
                            if (!aliasSet.contains(alias)) {
                                hql.append(LEFT_JOIN).append(eruptModel.getEruptName()).append(EruptConst.DOT).append(field.getName()).append(AS).append(field.getName());
                                aliasSet.add(alias);
                            }
                        }
                    }
                }
            }
        });
        return hql.toString();
    }

    public static String geneEruptHqlCondition(EruptModel eruptModel, List<Condition> conditions, List<String> customCondition) {
        StringBuilder hql = new StringBuilder();
        hql.append(" where 1 = 1 ");
        //condition
        if (null != conditions) {
            for (Condition condition : conditions) {
                EruptFieldModel eruptFieldModel = eruptModel.getEruptFieldMap().get(condition.getKey());
                if (null != eruptFieldModel) {
                    Edit edit = eruptFieldModel.getEruptField().edit();
                    if (edit.type() == EditType.REFERENCE_TREE) {
                        hql.append(EruptJpaUtils.AND).append(condition.getKey()).append(EruptConst.DOT).append(edit.referenceTreeType().id()).append("=:").append(condition.getKey());
                        continue;
                    } else if (edit.type() == EditType.REFERENCE_TABLE) {
                        hql.append(EruptJpaUtils.AND).append(condition.getKey()).append(EruptConst.DOT).append(edit.referenceTableType().id()).append("=:").append(condition.getKey());
                        continue;
                    }
                    String _key = EruptJpaUtils.completeHqlPath(eruptModel.getEruptName(), condition.getKey());

                    switch (condition.getExpression()) {
                        case EQ:
                            hql.append(EruptJpaUtils.AND).append(_key).append("=:").append(condition.getKey());
                            break;
                        case LIKE:
                            hql.append(EruptJpaUtils.AND).append(_key).append(" like :").append(condition.getKey());
                            break;
                        case RANGE:
                            hql.append(EruptJpaUtils.AND).append(_key).append(" between :")
                                    .append(L_VAL_KEY).append(condition.getKey()).append(" and :")
                                    .append(R_VAL_KEY).append(condition.getKey());
                            break;
                        case IN:
                            hql.append(EruptJpaUtils.AND).append(_key).append(" in (:").append(condition.getKey()).append(")");
                            break;
                    }
                } else {
                    hql.append(EruptJpaUtils.AND).append(condition.getKey()).append("=:").append(condition.getKey().replace(EruptConst.DOT, "_"));
                }
            }
        }
        for (Filter filter : eruptModel.getErupt().filter()) {
            Optional.ofNullable(filter.value()).ifPresent(it -> hql.append(AND).append(it));
        }
        Optional.ofNullable(customCondition).ifPresent(it -> it.forEach(str -> {
            if (StringUtils.isNotBlank(str)) hql.append(EruptJpaUtils.AND).append(str);
        }));
        return hql.toString();
    }

    public static String geneEruptHqlOrderBy(EruptModel eruptModel, String orderBy) {
        if (StringUtils.isNotBlank(orderBy)) {
            return " order by " + EruptJpaUtils.completeHqlPath(eruptModel.getEruptName(), orderBy);
        } else if (StringUtils.isNotBlank(eruptModel.getErupt().orderBy())) {
            return " order by " + EruptJpaUtils.completeHqlPath(eruptModel.getEruptName(), eruptModel.getErupt().orderBy());
        } else {
            return "";
        }
    }

    //在left join的情况下要求必须指定表信息，通过此方法生成；
    public static String completeHqlPath(String eruptName, String hqlPath) {
        if (hqlPath.contains(EruptConst.DOT)) {
            return hqlPath;
        } else {
            return eruptName + EruptConst.DOT + hqlPath;
        }
    }

}
