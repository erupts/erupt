package com.erupt.erupt;

import com.erupt.annotation.Erupt;
import com.erupt.annotation.EruptField;
import com.erupt.annotation.sub_field.View;
import com.erupt.annotation.util.ConfigUtil;
import com.erupt.model.EruptFieldModel;
import com.erupt.model.EruptModel;
import lombok.extern.java.Log;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by liyuepeng on 10/10/18.
 */
@Log
public class EruptAnnoConver {
    public static JSONObject converEruptJson(String eruptStr, Erupt erupt) {
        JSONObject eruptJson = new JSONObject(ConfigUtil.annoStrToJsonStr(eruptStr));

        return eruptJson;
    }

    public static void validateEruptFieldInfo(EruptFieldModel eruptFieldModel) {
        for (View view : eruptFieldModel.getEruptField().multiView()) {
            if ("".equals(view.name())) {
                throw new RuntimeException(eruptFieldModel.getField().getName() + "[" + view.title() + "]" + "-->multiView请指定name值");
            }
        }

        switch (eruptFieldModel.getEruptField().edit().type()) {
            case DICT:
                if ("".equals(eruptFieldModel.getEruptField().edit().dictType().dictCode())) {
                    throw new RuntimeException(eruptFieldModel.getField().getName() + "-->字典类型未提供字典编码");
                }
                break;
            case CHOICE:
                if (eruptFieldModel.getEruptField().edit().choiceType().values().length == 0) {
                    throw new RuntimeException(eruptFieldModel.getField().getName() + "-->单选/多选类型数据未提供可选值");
                }
                break;
            case REFERENCE:
                if ("".equals(eruptFieldModel.getEruptField().edit().referenceType().id())) {
                    throw new RuntimeException(eruptFieldModel.getField().getName() + "-->引用类型未填写引用ID");
                }
                break;

            case BOOLEAN:

                break;
        }
    }


    //生成列视图
    public static void shakeViewToLineView(EruptModel eruptModel) {
        JSONArray eruptViews = new JSONArray();
        eruptModel.getEruptFieldModels().forEach((eruptFieldModel) -> {
            EruptField eruptField = eruptFieldModel.getEruptField();
            if (eruptField.multiView().length != 0) {

                for (View view : eruptField.multiView()) {
                    JSONObject jsonObject = new JSONObject(ConfigUtil.annoStrToJsonStr(view.toString()));
                    jsonObject.put("name", eruptFieldModel.getField().getName() + "_" + view.name());
                    eruptViews.put(jsonObject);
                }
            } else {
                JSONObject jsonObject = new JSONObject(ConfigUtil.annoStrToJsonStr(eruptField.view().toString()));
                jsonObject.put("name", eruptFieldModel.getField().getName());
                eruptViews.put(jsonObject);
            }

        });
        eruptModel.setEruptViews(eruptViews);
    }

}
