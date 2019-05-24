package xyz.erupt.core.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.json.JSONObject;
import xyz.erupt.annotation.fun.ConditionHandler;
import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.core.annotation.EruptDataProcessor;
import xyz.erupt.core.service.DataService;
import xyz.erupt.core.service.data_impl.DBService;

import java.beans.Transient;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Created by liyuepeng on 2019-02-28.
 */
public class AnnotationUtil {

    private static final String[] ANNOTATION_NUMBER_TYPE = {
            "short", "int", "long", "float", "double"
    };

    private static final String[] ANNOTATION_STRING_TYPE = {
            "String", "byte", "char"
    };

    private static final String[] SIMPLE_ANNOTATION_TYPE = {
            "short", "int", "long", "float", "double", "byte", "char"
    };

    public static String annotationToJson(String annotationStr) {
        String convertStr = annotationStr
                .replaceAll("@xyz\\.erupt\\.annotation\\.sub_field\\.sub_edit\\.sub_attachment\\.\\w+", "")
                .replaceAll("@xyz\\.erupt\\.annotation\\.sub_field\\.sub_edit\\.\\w+", "")
                .replaceAll("@xyz\\.erupt\\.annotation\\.sub_field\\.sub_view\\.\\w+", "")
                .replaceAll("@xyz\\.erupt\\.annotation\\.sub_field\\.\\w+", "")
                .replaceAll("@xyz\\.erupt\\.annotation\\.sub_erupt\\.\\w+", "")
                .replaceAll("@xyz\\.erupt\\.annotation\\.\\w+", "")
                .replaceAll("class [a-zA-Z0-9.]+", "") //屏蔽类信息
                .replace("=,", "='',")
                .replace("=)", "='')")
                .replace("=", ":")
                .replace("(", "{")
                .replace(")", "}");
        return new JSONObject(convertStr).toString();
    }

    public static JsonObject annotationToJsonByReflect(Annotation annotation) {
        JsonObject jsonObject = new JsonObject();
        try {
            annotationMethodToObject(annotation, jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private static void annotationMethodToObject(Annotation annotation, JsonObject jsonObject) throws InvocationTargetException, IllegalAccessException {
        for (Method method : annotation.annotationType().getDeclaredMethods()) {
            Transient tran = method.getAnnotation(Transient.class);
            if (null != tran && tran.value()) continue;
            String returnType = method.getReturnType().getSimpleName();
            Object result = method.invoke(annotation);
            if (returnType.endsWith("[]")) {
                returnType = returnType.substring(0, returnType.length() - 2);
                JsonArray jsonArray = new JsonArray();
                //基本类型无法强转成Object类型数组，所以使用下面的方法进行处理
                if (Arrays.asList(ANNOTATION_NUMBER_TYPE).contains(returnType)) {
                    TypeUtil.simpleNumberTypeArrayToObject(result, returnType, (number) -> jsonArray.add(number));
                } else if ("char".equals(returnType)) {
                    char[] intArray = (char[]) result;
                    for (char i : intArray) {
                        jsonArray.add(i);
                    }
                } else if ("byte".equals(returnType)) {
                    byte[] intArray = (byte[]) result;
                    for (byte i : intArray) {
                        jsonArray.add(i);
                    }
                } else {
                    Object[] resultArray = (Object[]) result;
                    for (Object res : resultArray) {
                        if ("String".equals(returnType)) {
                            jsonArray.add(res.toString());
                        } else if ("boolean".equals(returnType)) {
                            jsonArray.add((boolean) res);
                        } else if ("Class".equals(returnType)) {
                            break;
                        } else if (res.getClass().isEnum()) {
                            jsonArray.add(res.toString());
                        } else {
                            JsonObject subJsonObject = new JsonObject();
                            annotationMethodToObject((Annotation) res, subJsonObject);
                            jsonArray.add(subJsonObject);
                        }
                    }
                }
                jsonObject.add(method.getName(), jsonArray);
            } else {
                if (Arrays.asList(ANNOTATION_STRING_TYPE).contains(returnType)) {
                    jsonObject.addProperty(method.getName(), result.toString());
                } else if (Arrays.asList(ANNOTATION_NUMBER_TYPE).contains(returnType)) {
                    jsonObject.addProperty(method.getName(), (Number) result);
                } else if ("Class".equals(returnType)) {
                    continue;
                } else if ("boolean".equals(returnType)) {
                    jsonObject.addProperty(method.getName(), (boolean) result);
                } else if (method.getReturnType().isEnum()) {
                    jsonObject.addProperty(method.getName(), result.toString());
                } else if (method.getReturnType().isAnnotation()) {
                    JsonObject subJsonObject = new JsonObject();
                    annotationMethodToObject((Annotation) result, subJsonObject);
                    jsonObject.add(method.getName(), subJsonObject);
                }
            }
        }
    }

    public static String switchFilterConditionToStr(Filter filter) {
        String condition = filter.condition();
        if (filter.conditionHandlers().length > 0) {
            for (Class<? extends ConditionHandler> conditionHandler : filter.conditionHandlers()) {
                ConditionHandler ch = SpringUtil.getBean(conditionHandler);
                condition = ch.handler(condition);
            }
        }
        return condition;
    }

    public static DataService getEruptDataProcessor(Class<?> clazz) {
        EruptDataProcessor eruptDataProcessor = clazz.getAnnotation(EruptDataProcessor.class);
        DataService dataService;
        if (null != eruptDataProcessor) {
            dataService = SpringUtil.getBean(eruptDataProcessor.processors());
        } else {
            dataService = SpringUtil.getBean(DBService.class);
        }
        return dataService;
    }

    public static void main(String[] args) {
        int[] a = {1, 2, 3, 4};
        for (int i : a) {
            Number b = i;
        }
    }

}
