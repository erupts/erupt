package xyz.erupt.core.view;

import com.google.gson.JsonObject;
import lombok.Data;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.core.util.AnnotationUtil;

import java.util.List;
import java.util.Map;

/**
 * @author liyuepeng
 * @date 2018-09-29.
 */
@Data
public final class EruptModel implements Cloneable {

    private transient Class<?> clazz;

    private transient Erupt erupt;

    private String eruptName;

    private JsonObject eruptJson;

    private List<EruptFieldModel> eruptFieldModels;

    private transient Map<String, EruptFieldModel> eruptFieldMap;

    public EruptModel(Class<?> eruptClazz) {
        this.clazz = eruptClazz;
        this.erupt = eruptClazz.getAnnotation(Erupt.class);
        this.eruptName = eruptClazz.getSimpleName();
        this.eruptJson = AnnotationUtil.annotationToJsonByReflect(this.erupt);
    }

    @Override
    public EruptModel clone() throws CloneNotSupportedException {
        return (EruptModel) super.clone();
    }

}
