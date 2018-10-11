package com.erupt.model;

import com.erupt.annotation.EruptField;
import com.erupt.annotation.util.ConfigUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * Created by liyuepeng on 10/10/18.
 */
public class EruptFieldModel implements Serializable {

    private transient EruptField eruptField;

    private transient Field field;

    private JsonObject eruptFieldJson;


    public void setEruptField(EruptField eruptField) {
        this.eruptField = eruptField;

        this.eruptFieldJson = new JsonParser().parse(ConfigUtil.annoStrToJsonStr(eruptField.toString())).getAsJsonObject();
    }

    public EruptField getEruptField() {
        return eruptField;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public JsonObject getEruptFieldJson() {
        return eruptFieldJson;
    }
}
