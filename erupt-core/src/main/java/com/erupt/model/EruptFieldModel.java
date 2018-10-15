package com.erupt.model;

import com.erupt.annotation.EruptField;
import com.erupt.annotation.util.ConfigUtil;
import com.erupt.util.EruptAnnoConver;
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

    private String fieldName;

    private String fieldReturnName;

    public EruptFieldModel(EruptField eruptField, Field field) {
        this.eruptField = eruptField;
        this.field = field;
        this.eruptFieldJson = new JsonParser().parse(ConfigUtil.annoStrToJsonStr(eruptField.toString())).getAsJsonObject();
        this.fieldName = field.getName();
        this.fieldReturnName = field.getType().getSimpleName();
        EruptAnnoConver.validateEruptFieldInfo(this);
    }

    public EruptFieldModel() {

    }

    public void setEruptField(EruptField eruptField) {
        this.eruptField = eruptField;
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

    public void setEruptFieldJson(JsonObject eruptFieldJson) {
        this.eruptFieldJson = eruptFieldJson;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldReturnName() {
        return fieldReturnName;
    }

    public void setFieldReturnName(String fieldReturnName) {
        this.fieldReturnName = fieldReturnName;
    }
}
