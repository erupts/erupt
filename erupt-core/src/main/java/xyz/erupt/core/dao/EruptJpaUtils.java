package xyz.erupt.core.dao;

import org.apache.commons.lang3.StringUtils;
import xyz.erupt.annotation.constant.JavaType;
import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.core.query.Condition;
import xyz.erupt.core.query.Query;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.util.AnnotationUtil;
import xyz.erupt.core.util.ReflectUtil;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;

import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author liyuepeng
 * @date 2018-11-05.
 */
public class EruptJpaUtils {

    public static final String AND = " and ";

    public static final String LVAL_KEY = "l_";

    public static final String RVAL_KEY = "r_";

    public static final String NULL = "$null$";

    public static final String NOT_NULL = "$notNull$";

    public static final String PERCENT = "%";

    public static Set<String> getEruptColJapKeys(EruptModel eruptModel) {
        Set<String> cols = new HashSet<>();
        String eruptNameSymbol = eruptModel.getEruptName() + ".";
        cols.add(eruptNameSymbol + eruptModel.getErupt().primaryKeyCol() + " as " + eruptModel.getErupt().primaryKeyCol());
        eruptModel.getEruptFieldModels().forEach(field -> {
            if (null != field.getField().getAnnotation(OneToMany.class)) {
                return;
            }
            if (null != field.getField().getAnnotation(Transient.class)) {
                return;
            }
            String fieldKey;
            for (View view : field.getEruptField().views()) {
                if (view.column().length() == 0) {
                    fieldKey = eruptNameSymbol + field.getFieldName() + " as " + field.getFieldName();
                } else {
                    fieldKey = eruptNameSymbol + field.getFieldName() + "." + view.column() + " as " + field.getFieldName() + "_"
                            + view.column().replace(".", "_");
                }
                cols.add(fieldKey);
            }
        });
        return cols;
    }


    //erupt 注解信息映射成hql语句
    public static String generateEruptJpaHql(EruptModel eruptModel, String cols, Query query, boolean countSql) {
        StringBuilder hql = new StringBuilder();
        if (StringUtils.isNotBlank(cols)) {
            hql.append("select ");
            hql.append(cols);
            hql.append(" from ").
                    append(eruptModel.getEruptName()).
                    append(" as ").
                    append(eruptModel.getEruptName());
            ReflectUtil.findClassAllFields(eruptModel.getClazz(), field -> {
                if (null != field.getAnnotation(ManyToOne.class) ||
                        null != field.getAnnotation(OneToOne.class)) {
                    hql.append(" left outer join ").append(eruptModel.getEruptName()).append(".")
                            .append(field.getName() + " as " + field.getName());
                }
            });

        } else {
            hql.append("from " + eruptModel.getEruptName());
        }
        hql.append(geneEruptHqlCondition(eruptModel, query.getConditions(), query.getConditionStrings()));
        if (!countSql) {
            hql.append(geneEruptHqlOrderBy(eruptModel, query.getOrderBy()));
        }
        return hql.toString();
    }


    public static String generateEruptJoinHql(EruptModel eruptModel) {
        StringBuilder sb = new StringBuilder();
        ReflectUtil.findClassAllFields(eruptModel.getClazz(), field -> {
            if (null != field.getAnnotation(ManyToOne.class) || null != field.getAnnotation(OneToOne.class)) {
                sb.append(" left outer join ")
                        .append(eruptModel.getEruptName()).append(".").append(field.getName()).append(" as ").append(field.getName());
                try {
                    Object obj = field.get(eruptModel.getClazz());
                    String join = generateEruptJoinHql(EruptCoreService.getErupt(obj.getClass().getSimpleName()));
                    sb.append(join);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        });
        return sb.toString();
    }

    public static String geneEruptHqlCondition(EruptModel eruptModel, List<Condition> clientSearchCondition, List<String> customCondition) {
        StringBuilder hql = new StringBuilder();
        hql.append(" where 1 = 1 ");
        //condition
        if (null != clientSearchCondition) {
            for (Condition condition : clientSearchCondition) {
                EruptFieldModel eruptFieldModel = eruptModel.getEruptFieldMap().get(condition.getKey());
                if (null != eruptFieldModel) {
                    Edit edit = eruptFieldModel.getEruptField().edit();
                    if (edit.type() == EditType.REFERENCE_TREE) {
                        hql.append(EruptJpaUtils.AND).append(condition.getKey() + "." + edit.referenceTreeType().id()).append("=:").append(condition.getKey());
                        continue;
                    } else if (edit.type() == EditType.REFERENCE_TABLE) {
                        hql.append(EruptJpaUtils.AND).append(condition.getKey() + "." + edit.referenceTableType().id()).append("=:").append(condition.getKey());
                        continue;
                    }
                    String $key = EruptJpaUtils.completeHqlPath(eruptModel.getEruptName(), condition.getKey());
                    if (edit.search().vague()) {
                        if ((edit.type() == EditType.NUMBER)
                                || edit.type() == EditType.DATE
                                || edit.type() == EditType.SLIDER) {
                            hql.append(EruptJpaUtils.AND).append($key)
                                    .append(" between :").append(LVAL_KEY).append(condition.getKey())
                                    .append(" and :").append(RVAL_KEY).append(condition.getKey());
                        } else if (edit.type() == EditType.CHOICE) {
                            hql.append(EruptJpaUtils.AND).append($key).append(" in (:").append(condition.getKey()).append(")");
                        } else if (eruptFieldModel.getFieldReturnName().equals(JavaType.STRING)) {
                            hql.append(EruptJpaUtils.AND).append($key).append(" like :").append(condition.getKey());
                        } else {
                            hql.append(EruptJpaUtils.AND).append($key).append("=:").append(condition.getKey());
                        }
                    } else {
                        hql.append(EruptJpaUtils.AND).append($key).append("=:").append(condition.getKey());
                    }
                } else {
                    hql.append(EruptJpaUtils.AND).append(condition.getKey()).append("=:").append(condition.getKey());
                }
            }
        }
        for (Filter filter : eruptModel.getErupt().filter()) {
            String filterStr = AnnotationUtil.switchFilterConditionToStr(filter);
            if (StringUtils.isNotBlank(filterStr)) {
                hql.append(AND).append(filterStr);
            }
        }
        if (null != customCondition) {
            for (String s : customCondition) {
                if (StringUtils.isNotBlank(s)) {
                    hql.append(EruptJpaUtils.AND).append(s);
                }
            }
        }
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
        if (hqlPath.contains(".")) {
            return hqlPath;
        } else {
            return eruptName + "." + hqlPath;
        }

    }


}
