package xyz.erupt.core.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.core.model.EruptFieldModel;
import xyz.erupt.core.model.EruptModel;

/**
 * Created by liyuepeng on 2019-05-31.
 */
public class EruptValidate {

    public static void validate(EruptModel eruptModel, JsonObject jsonObject) {
        for (EruptFieldModel model : eruptModel.getEruptFieldModels()) {
            EditType editType = model.getEruptField().edit().type();
            JsonElement jsonElement = jsonObject.get(model.getFieldName());
            if (null != jsonElement) {
                switch (editType) {
                    case REFERENCE_TREE:
                        jsonElement.getAsJsonObject().get(model.getEruptField().edit().referenceTreeType().id());

                        int a = 1 + 1;
                        break;
                    default:
                        break;
                }
            }

        }

    }
}
