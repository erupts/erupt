package com.erupt.util;

import com.erupt.model.EruptFieldModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liyuepeng on 11/1/18.
 */
public class EruptUtil {

    public static List<String> getEruptFieldNames(List<EruptFieldModel> eruptFieldModels) {
        List<String> fieldNames = new ArrayList<>();
        eruptFieldModels.forEach(field -> fieldNames.add(field.getFieldName()));
        return fieldNames;
    }
}
