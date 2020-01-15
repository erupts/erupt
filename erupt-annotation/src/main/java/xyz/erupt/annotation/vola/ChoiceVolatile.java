package xyz.erupt.annotation.vola;

import com.google.gson.JsonElement;
import xyz.erupt.annotation.config.VolatileFun;
import xyz.erupt.annotation.sub_field.sub_edit.VL;

/**
 * @author liyuepeng
 * @date 2020-01-15
 */
public class ChoiceVolatile implements VolatileFun<VL[]> {

    @Override
    public JsonElement exec(VL[] vl) {
        for (VL vl1 : vl) {
            System.out.println(vl1.label());
        }
        return null;
    }
}
