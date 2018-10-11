package com.erupt.model;

import com.erupt.annotation.EruptField;
import com.erupt.annotation.util.ConfigUtil;
import org.json.JSONObject;

import java.lang.reflect.Field;

/**
 * Created by liyuepeng on 10/10/18.
 */
public class EruptFieldModel {

    private EruptField eruptField;

    private Field field;

    private JSONObject eruptFieldJson;

    public EruptField getEruptField() {
        return eruptField;
    }

    public void setEruptField(EruptField eruptField) {
        this.eruptField = eruptField;
        this.eruptFieldJson = new JSONObject(ConfigUtil.annoStrToJsonStr(eruptField.toString()));
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public JSONObject getEruptFieldJson() {
        return eruptFieldJson;
    }
}
