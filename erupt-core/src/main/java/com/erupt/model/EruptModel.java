package com.erupt.model;

import com.erupt.annotation.Erupt;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by liyuepeng on 9/29/18.
 */
public class EruptModel {

    private Class<?> clazz;

    private Erupt erupt;

    private JSONArray eruptViews;

    private JSONObject eruptJson;

    List<EruptFieldModel> eruptFieldModels;

    public JSONObject getEruptJson() {
        return eruptJson;
    }

    public void setEruptJson(JSONObject eruptJson) {
        this.eruptJson = eruptJson;
    }

    public List<EruptFieldModel> getEruptFieldModels() {
        return eruptFieldModels;
    }

    public void setEruptFieldModels(List<EruptFieldModel> eruptFieldModels) {
        this.eruptFieldModels = eruptFieldModels;
    }

    public Erupt getErupt() {
        return erupt;
    }

    public void setErupt(Erupt erupt) {
        this.erupt = erupt;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    public JSONArray getEruptViews() {
        return eruptViews;
    }

    public void setEruptViews(JSONArray eruptViews) {
        this.eruptViews = eruptViews;
    }
}
