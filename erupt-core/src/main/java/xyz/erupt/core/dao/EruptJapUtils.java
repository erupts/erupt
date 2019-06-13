package xyz.erupt.core.dao;

import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.core.model.EruptFieldModel;
import xyz.erupt.core.model.EruptModel;
import xyz.erupt.core.model.HqlModel;
import xyz.erupt.core.util.AnnotationUtil;

import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by liyuepeng on 11/5/18.
 */
public class EruptJapUtils {

    public static final String AND = " and ";

    public static final String LVAL_KEY = "l_";

    public static final String RVAL_KEY = "r_";

    public static final String NULL = "$null$";

    public static final String NOT_NULL = "$notNull$";

    public static Set<String> getEruptColJapKeys(EruptModel eruptModel) {
        Set<String> cols = new HashSet<>();
        String eruptNameSymbol = eruptModel.getEruptName() + ".";
        cols.add(eruptNameSymbol + eruptModel.getErupt().primaryKeyCol() + " as " + eruptModel.getErupt().primaryKeyCol());
        eruptModel.getEruptFieldModels().forEach(field -> {
            if (field.getEruptField().edit().type() == EditType.TAB) {
                return;
            }
            if (null != field.getField().getAnnotation(Transient.class)) {
                return;
            }
            String fieldKey;
            for (View view : field.getEruptField().views()) {
                if ("".equals(view.column())) {
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
    public static String generateEruptJpaHql(EruptModel eruptModel, HqlModel hqlModel) {
        StringBuilder hql = new StringBuilder();
        if (StringUtils.isNotBlank(hqlModel.getCols())) {
            hql.append("select ");
            hql.append(hqlModel.getCols());
            hql.append(" from ").
                    append(eruptModel.getEruptName()).
                    append(" ").
                    append(eruptModel.getEruptName());
            eruptModel.getEruptFieldModels().forEach(field -> {
                if (null != field.getField().getAnnotation(ManyToOne.class) || null != field.getField().getAnnotation(OneToOne.class)) {
                    hql.append(" left outer join ").append(eruptModel.getEruptName()).append(".").append(field.getFieldName()).append(" ").append(field.getFieldName());
                }
            });
        } else {
            hql.append("from " + eruptModel.getEruptName());
        }
        hql.append(" where 1=1");
        Filter filter = eruptModel.getErupt().filter();
        if (!"".equals(filter.condition())) {
            hql.append(AND).append(AnnotationUtil.switchFilterConditionToStr(filter));
        }
        //condition
        JsonObject condition = hqlModel.getEruptSearchCondition();
        if (null != condition) {
            for (String key : condition.keySet()) {
                if (!condition.get(key).isJsonNull()) {
                    EruptFieldModel eruptFieldModel = eruptModel.getEruptFieldMap().get(key);
                    if (null != eruptFieldModel) {
                        if (eruptFieldModel.getEruptField().edit().search().value()) {
                            Edit edit = eruptFieldModel.getEruptField().edit();
                            String _key = EruptJapUtils.completeHqlPath(eruptModel.getEruptName(), key);
                            if (edit.search().vague()) {
                                if ((edit.type() == EditType.INPUT && eruptFieldModel.getFieldReturnName().equals(EruptFieldModel.NUMBER_TYPE))
                                        || edit.type() == EditType.DATE
                                        || edit.type() == EditType.SLIDER) {
                                    hql.append(EruptJapUtils.AND).append(_key)
                                            .append(" >=:").append(LVAL_KEY + key)
                                            .append(EruptJapUtils.AND).append(_key)
                                            .append(" <=:").append(RVAL_KEY + key);
                                    continue;
                                } else if (edit.type() == EditType.INPUT && eruptFieldModel.getFieldReturnName().equals("String")) {
                                    hql.append(EruptJapUtils.AND).append(_key).append(" like :").append(key);
                                    continue;
                                } else if (edit.type() == EditType.CHOICE) {
                                    hql.append(EruptJapUtils.AND).append(_key).append(" in :").append(key);
                                    continue;
                                }
                            }
                            if (edit.type() == EditType.REFERENCE_TREE) {
                                hql.append(EruptJapUtils.AND).append(key + "." + edit.referenceTreeType().id()).append("=:").append(key);
                                continue;
                            }
                            if (condition.get(key).toString().contains(EruptJapUtils.NULL)) {
                                hql.append(EruptJapUtils.AND).append(_key).append(" is null");
                            } else if (condition.get(key).toString().contains(EruptJapUtils.NOT_NULL)) {
                                hql.append(EruptJapUtils.AND).append(_key).append(" is not null");
                            } else {
                                hql.append(EruptJapUtils.AND).append(_key).append("=:").append(key);
                            }
                        }
                    }
                }
            }
        }
        String customCondition = hqlModel.getCustomCondition();
        if (StringUtils.isNotBlank(customCondition)) {
            hql.append(EruptJapUtils.AND).append(customCondition);
        }
        //sort
        if (StringUtils.isNotBlank(hqlModel.getOrderBy())) {
            hql.append(" order by ").append(hqlModel.getOrderBy());
        } else if (eruptModel.getErupt().sorts().length > 0) {
            String[] sorts = eruptModel.getErupt().sorts();
            hql.append(" order by ").append(convertSorts(eruptModel.getEruptName(), sorts));
        }
        //TODO
        System.err.println(hql.toString());
        return hql.toString();
    }

    private static String convertSorts(String eruptName, String[] sorts) {
        for (int i = 0; i < sorts.length; i++) {
            if (!sorts[i].contains(".")) {
                sorts[i] = eruptName + '.' + sorts[i];
            }
        }
        return String.join(",", sorts);
    }

    //在left join的情况下要求必须指定表信息，通过此方法生成；
    public static String completeHqlPath(String eruptName, String hqlPath) {
        if (hqlPath.contains(".")) {
            return hqlPath;
        } else {
            return eruptName + "." + hqlPath;
        }

    }

    public static String generateEruptJapCondition(EruptModel eruptModel) {

        return null;
    }


}
