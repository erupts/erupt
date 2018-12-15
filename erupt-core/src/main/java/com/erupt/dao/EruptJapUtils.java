package com.erupt.dao;

import com.erupt.annotation.sub_field.EditType;
import com.erupt.annotation.sub_field.View;
import com.erupt.annotation.sub_field.sub_edit.ReferenceType;
import com.erupt.base.model.EruptModel;
import com.erupt.util.TypeUtil;

import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by liyuepeng on 11/5/18.
 */
public class EruptJapUtils {
    public static Set<String> getEruptColJapKeys(EruptModel eruptModel) {
        Set<String> fieldKeys = new HashSet<>();
        String eruptNameSymbol = eruptModel.getEruptName() + ".";
        eruptModel.getEruptFieldModels().forEach(field -> {
            if (field.getEruptField().edit().type() == EditType.TAB) {
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
                fieldKeys.add(fieldKey);
            }
            if (field.getEruptField().edit().type() == EditType.REFERENCE) {
                ReferenceType referenceType = field.getEruptField().edit().referenceType()[0];
                fieldKey = eruptNameSymbol + field.getFieldName() + "." + referenceType.id() + " as " + field.getFieldName() + "_" + referenceType.id();
                fieldKeys.add(fieldKey);
                fieldKey = eruptNameSymbol + field.getFieldName() + "." + referenceType.label() + " as " + field.getFieldName() + "_" + referenceType.label();
                fieldKeys.add(fieldKey);
            }
            if (field.getEruptField().views().length == 0) {
                if (field.getEruptField().edit().type() != EditType.REFERENCE) {
                    if (TypeUtil.isFieldSimpleType(field.getField().getType().getSimpleName())) {
                        fieldKey = eruptNameSymbol + field.getFieldName() + " as " + field.getFieldName();
                        fieldKeys.add(fieldKey);
                    }
                }
            }
        });
        return fieldKeys;
    }


    public static String generateEruptJpaHql(EruptModel eruptModel) {
        StringBuilder hql = new StringBuilder("select new map(");
        hql.append(String.join(",", getEruptColJapKeys(eruptModel)));
        hql.append(") from ").
                append(eruptModel.getEruptName()).
                append(" ").
                append(eruptModel.getEruptName());
        eruptModel.getEruptFieldModels().forEach(field -> {
            if (null != field.getField().getAnnotation(ManyToOne.class) || null != field.getField().getAnnotation(OneToOne.class)) {
                hql.append(" left join ").append(eruptModel.getEruptName()).append(".").append(field.getFieldName()).append(" ").append(field.getFieldName());
            }
        });
        hql.append(" ");
        return hql.toString();
    }

    public static String generateEruptJapCondition(EruptModel eruptModel) {

        return null;
    }


}
