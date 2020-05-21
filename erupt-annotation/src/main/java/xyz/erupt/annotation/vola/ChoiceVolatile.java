package xyz.erupt.annotation.vola;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.SneakyThrows;
import xyz.erupt.annotation.config.VolatileFun;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.VL;

import java.util.Map;

/**
 * @author liyuepeng
 * @date 2020-01-15
 */
@Deprecated
public class ChoiceVolatile implements VolatileFun<VL[], ChoiceType> {

    private static final String LABEL = "label";

    private static final String VALUE = "value";

    @Override
    @SneakyThrows
    public JsonElement exec(VL[] vl, ChoiceType choiceType) {
        JsonArray jsonArray = new JsonArray();
        for (Class<? extends ChoiceFetchHandler> handler : choiceType.fetchHandler()) {
            Map<String, String> map = handler.newInstance().fetch(choiceType.fetchHandlerParams());
            for (Map.Entry<String, String> entry : map.entrySet()) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty(LABEL, entry.getValue());
                jsonObject.addProperty(VALUE, entry.getKey());
                jsonArray.add(jsonObject);
            }
        }
        for (VL v : vl) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(LABEL, v.label());
            jsonObject.addProperty(VALUE, v.value());
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }
}
