package com.erupt.dao;

import com.erupt.annotation.sub_field.View;
import com.erupt.model.EruptFieldModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liyuepeng on 11/5/18.
 */
public class EruptJapUtils {
    public static List<String> getEruptColJapKeys(List<EruptFieldModel> eruptFieldModels) {
        List<String> fieldKeys = new ArrayList<>();
        eruptFieldModels.forEach(field -> {
            for (View view : field.getEruptField().views()) {
                String fieldKey;
                if ("".equals(view.column())) {
                    fieldKey = field.getFieldName() + " as " + field.getFieldName();
                } else {
                    fieldKey = field.getFieldName() + "." + view.column() + " as " + field.getFieldName() + "_"
                            + view.column().replace(".", "_");
                }
                fieldKeys.add(fieldKey);
            }
        });
        return fieldKeys;
    }
}
