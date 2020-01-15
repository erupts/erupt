package xyz.erupt.annotation.vola;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import xyz.erupt.annotation.config.VolatileFun;
import xyz.erupt.annotation.sub_erupt.Power;

/**
 * @author liyuepeng
 * @date 2020-01-07
 */
public class PowerVolatile implements VolatileFun<Power> {

    private static final String ADD = "add";

    private static final String EDIT = "edit";

    private static final String DELETE = "delete";

    private static final String QUERY = "query";

    private static final String VIEW_DETAILS = "viewDetails";

    private static final String EXPORT = "export";

    private static final String IMPORTABLE = "importable";

    @Override
    public JsonElement exec(Power power) {
        JsonObject jsonElement = new JsonObject();
        xyz.erupt.annotation.fun.PowerHandler.PowerBean powerBean =
                new xyz.erupt.annotation.fun.PowerHandler.PowerBean(power);
        if (!power.powerHandler().isInterface()) {
            try {
                power.powerHandler().newInstance().handler(powerBean);
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        jsonElement.addProperty(ADD, powerBean.isAdd());
        jsonElement.addProperty(EDIT, powerBean.isEdit());
        jsonElement.addProperty(DELETE, powerBean.isDelete());
        jsonElement.addProperty(QUERY, powerBean.isQuery());
        jsonElement.addProperty(VIEW_DETAILS, powerBean.isViewDetails());
        jsonElement.addProperty(EXPORT, powerBean.isExport());
        jsonElement.addProperty(IMPORTABLE, powerBean.isImportable());
        return jsonElement;
    }

}
