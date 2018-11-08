package com.erupt.dao;

import com.erupt.annotation.sub_field.EditType;
import com.erupt.annotation.sub_field.View;
import com.erupt.annotation.sub_field.sub_edit.ReferenceType;
import com.erupt.model.EruptFieldModel;
import com.erupt.util.TypeUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by liyuepeng on 11/5/18.
 */
public class EruptJapUtils {
    public static Set<String> getEruptColJapKeys(List<EruptFieldModel> eruptFieldModels) {
        Set<String> fieldKeys = new HashSet<>();
        eruptFieldModels.forEach(field -> {
            if (field.getEruptField().edit().type() == EditType.TAB) {
                return;
            }
            String fieldKey;
            for (View view : field.getEruptField().views()) {
                if ("".equals(view.column())) {
                    fieldKey = field.getFieldName() + " as " + field.getFieldName();
                } else {
                    fieldKey = field.getFieldName() + "." + view.column() + " as " + field.getFieldName() + "_"
                            + view.column().replace(".", "_");
                }
                fieldKeys.add(fieldKey);
            }
            if (field.getEruptField().views().length == 0) {
                if (TypeUtil.isFieldSimpleType(field.getField().getType().getSimpleName())) {
                    fieldKey = field.getFieldName() + " as " + field.getFieldName();
                    fieldKeys.add(fieldKey);
                }
            }
            if (field.getEruptField().edit().type() == EditType.REFERENCE) {
                ReferenceType referenceType = field.getEruptField().edit().referenceType()[0];
                fieldKey = field.getFieldName() + "." + referenceType.id() + " as " + field.getFieldName() + "_" + referenceType.id();
                fieldKeys.add(fieldKey);
                fieldKey = field.getFieldName() + "." + referenceType.label() + " as " + field.getFieldName() + "_" + referenceType.label();
                fieldKeys.add(fieldKey);
            }
        });
        return fieldKeys;
    }
}
