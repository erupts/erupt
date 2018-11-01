package com.erupt.util;

import com.erupt.annotation.Erupt;
import com.erupt.annotation.EruptField;
import com.erupt.annotation.sub_field.View;
import com.erupt.annotation.util.ConfigUtil;
import com.erupt.constant.EruptConst;
import com.erupt.model.EruptFieldModel;
import com.erupt.model.EruptModel;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.fusesource.jansi.Ansi;
import org.json.JSONObject;

import static org.fusesource.jansi.Ansi.ansi;

/**
 * Created by liyuepeng on 10/10/18.
 */
public class EruptAnnoConver {
    public static JSONObject converEruptJson(String eruptStr, Erupt erupt) {

        return null;
    }




//    //生成展示列视图
//    public static void gcEruptFieldViews(EruptModel eruptModel) {
//        JsonArray eruptViews = new JsonArray();
//        eruptModel.getEruptFieldModels().forEach((eruptFieldModel) -> {
//            EruptField eruptField = eruptFieldModel.getEruptField();
//            if (eruptField.multiView().length != 0) {
//                for (View view : eruptField.multiView()) {
//                    JsonObject jsonObject = new JsonParser().parse(ConfigUtil.annoStrToJsonStr(view.toString())).getAsJsonObject();
//                    jsonObject.addProperty("name", eruptFieldModel.getField().getName() + "_" + view.name());
//                    eruptViews.add(jsonObject);
//                }
//            } else {
//                JsonObject jsonObject = new JsonParser().parse(ConfigUtil.annoStrToJsonStr(eruptField.view().toString())).getAsJsonObject();
//                jsonObject.addProperty("name", eruptFieldModel.getField().getName());
//                eruptViews.add(jsonObject);
//            }
//
//        });
//        eruptModel.setEruptFieldViews(eruptViews);
//    }

}
