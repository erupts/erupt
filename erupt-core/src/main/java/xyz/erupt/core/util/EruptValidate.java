package xyz.erupt.core.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import xyz.erupt.annotation.model.BoolAndReason;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.core.model.EruptFieldModel;
import xyz.erupt.core.model.EruptModel;

/**
 * Created by liyuepeng on 2019-05-31.
 */
public class EruptValidate {

    public static BoolAndReason validate(EruptModel eruptModel, JsonObject jsonObject) {
        for (EruptFieldModel field : eruptModel.getEruptFieldModels()) {
            if (field.getEruptField().edit().notNull()) {
                if (!jsonObject.has(field.getFieldName())) {
                    return new BoolAndReason(false, field.getEruptField().edit().title() + "必填");
                }
            }
            EditType editType = field.getEruptField().edit().type();
            JsonElement jsonElement = jsonObject.get(field.getFieldName());
            if (null != jsonElement) {
                switch (editType) {
                    case REFERENCE_TREE:

                        break;
                    default:
                        break;
                }
            }

        }
        return new BoolAndReason(true, "");
    }
}
