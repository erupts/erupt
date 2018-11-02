package com.erupt.util;

import com.erupt.annotation.EruptField;
import com.erupt.annotation.sub_field.EditType;
import com.erupt.annotation.sub_field.View;
import com.erupt.model.EruptFieldModel;
import com.erupt.model.EruptModel;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by liyuepeng on 11/1/18.
 */
public class EruptUtil {

    public static List<String> getEruptFieldNames(List<EruptFieldModel> eruptFieldModels) {
        List<String> fieldNames = new ArrayList<>();
        eruptFieldModels.forEach(field -> fieldNames.add(field.getFieldName()));
        return fieldNames;
    }



    //过滤非展示项字段信息
    public static void filterNoEruptFieldValue(Object o, Consumer<Field> consumer) {
        Field[] fields = o.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            EruptField eruptField = field.getAnnotation(EruptField.class);
            if (null == eruptField) {
                try {
                    field.set(o, null);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            } else {
                consumer.accept(field);
            }
        }
    }


    public static void converEruptFieldInfo(Object o) {
        filterNoEruptFieldValue(o, field -> {
            EruptField eruptField = field.getAnnotation(EruptField.class);
            if (!TypeUtil.isFieldSimpleType(field.getType().getSimpleName())) {
//                Field[] subFields = field.getType().getFields();
//                for (View view : eruptField.views()) {
//                    for (Field subField : subFields) {
//                        if (!subField.getName().equals(view.column())) {
//                            field.setAccessible(true);
//                            try {
//                                field.set(o, null);
//                            } catch (IllegalAccessException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                }
//                switch (eruptField.edit().type()) {
//                    case REFERENCE:
//                        eruptField.edit().referenceType()[0].id();
//                        eruptField.edit().referenceType()[0].label();
//                        break;
//                    case BOOLEAN:
//                        break;
//                }
            }
        });
    }

}
