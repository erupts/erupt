//package xyz.erupt.annotation.vola;
//
//import com.google.gson.JsonElement;
//import com.google.gson.JsonObject;
//import xyz.erupt.annotation.Erupt;
//import xyz.erupt.annotation.config.VolatileFun;
//import xyz.erupt.annotation.fun.PowerObject;
//import xyz.erupt.annotation.sub_erupt.Power;
//
///**
// * @author liyuepeng
// * @date 2020-01-07
// */
//@Deprecated
//public class PowerVolatile implements VolatileFun<Power, Erupt> {
//
//    private static final String ADD = "add";
//
//    private static final String EDIT = "edit";
//
//    private static final String DELETE = "delete";
//
//    private static final String QUERY = "query";
//
//    private static final String VIEW_DETAILS = "viewDetails";
//
//    private static final String EXPORT = "export";
//
//    private static final String IMPORTABLE = "importable";
//
//    @Override
//    public JsonElement exec(Power power, Erupt erupt) {
//        JsonObject jsonElement = new JsonObject();
//        PowerObject powerObj = new PowerObject(power);
//        if (!power.powerHandler().isInterface()) {
//            try {
//                power.powerHandler().newInstance().handler(powerObj);
//            } catch (InstantiationException | IllegalAccessException e) {
//                e.printStackTrace();
//            }
//        }
//        jsonElement.addProperty(ADD, powerObj.isAdd());
//        jsonElement.addProperty(EDIT, powerObj.isEdit());
//        jsonElement.addProperty(DELETE, powerObj.isDelete());
//        jsonElement.addProperty(QUERY, powerObj.isQuery());
//        jsonElement.addProperty(VIEW_DETAILS, powerObj.isViewDetails());
//        jsonElement.addProperty(EXPORT, powerObj.isExport());
//        jsonElement.addProperty(IMPORTABLE, powerObj.isImportable());
//        return jsonElement;
//    }
//
//}
