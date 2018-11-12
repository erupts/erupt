package com.erupt.model;

import com.erupt.annotation.Erupt;
import com.erupt.annotation.util.ConfigUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.persistence.Id;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * Created by liyuepeng on 9/29/18.
 */
public class EruptModel implements Serializable {

    private transient Class<?> clazz;

    private transient Erupt erupt;

    private String eruptName;

    private JsonObject eruptJson;

    //标识主键列
    private String primaryKeyCol;

    private List<EruptFieldModel> eruptFieldModels;

    private transient Map<String, EruptFieldModel> eruptFieldMap;

    public EruptModel(Class<?> eruptClazz) {
        this.clazz = eruptClazz;
        this.erupt = eruptClazz.getAnnotation(Erupt.class);
        this.eruptName = eruptClazz.getSimpleName();
        this.eruptJson = new JsonParser().parse(ConfigUtil.annoStrToJsonStr(erupt.toString())).getAsJsonObject();

    }


    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    public Erupt getErupt() {
        return erupt;
    }

    public void setErupt(Erupt erupt) {
        this.erupt = erupt;
    }

    public JsonObject getEruptJson() {
        return eruptJson;
    }

    public void setEruptJson(JsonObject eruptJson) {
        this.eruptJson = eruptJson;
    }

    public List<EruptFieldModel> getEruptFieldModels() {
        return eruptFieldModels;
    }

    public void setEruptFieldModels(List<EruptFieldModel> eruptFieldModels) {
        this.eruptFieldModels = eruptFieldModels;
    }

    public String getEruptName() {
        return eruptName;
    }

    public void setEruptName(String eruptName) {
        this.eruptName = eruptName;
    }

    public String getPrimaryKeyCol() {
        return primaryKeyCol;
    }

    public void setPrimaryKeyCol(Field field) {
        if (null != field.getAnnotation(Id.class)) {
            this.primaryKeyCol = field.getName();
        }
    }

    public void setPrimaryKeyCol(String primaryKeyCol) {
        this.primaryKeyCol = primaryKeyCol;
    }

    public Map<String, EruptFieldModel> getEruptFieldMap() {
        return eruptFieldMap;
    }

    public void setEruptFieldMap(Map<String, EruptFieldModel> eruptFieldMap) {
        this.eruptFieldMap = eruptFieldMap;
    }
}
