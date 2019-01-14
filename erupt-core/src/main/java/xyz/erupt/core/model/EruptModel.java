package xyz.erupt.core.model;

import lombok.Data;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.util.ConfigUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by liyuepeng on 9/29/18.
 */
@Data
public class EruptModel implements Serializable {

    private transient Class<?> clazz;

    private transient Erupt erupt;

    private String eruptName;

    private JsonObject eruptJson;

    private List<EruptFieldModel> eruptFieldModels;

    private List<EruptAndEruptFieldModel> subErupts;

    private transient Map<String, EruptFieldModel> eruptFieldMap;

    public EruptModel(Class<?> eruptClazz) {
        this.clazz = eruptClazz;
        this.erupt = eruptClazz.getAnnotation(Erupt.class);
        this.eruptName = eruptClazz.getSimpleName();
        this.eruptJson = new JsonParser().parse(ConfigUtil.annoStrToJsonStr(erupt.toString())).getAsJsonObject();
    }

    public EruptModel() {
    }
}
