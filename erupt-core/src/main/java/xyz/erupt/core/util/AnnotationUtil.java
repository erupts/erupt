package xyz.erupt.core.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.json.JSONObject;
import xyz.erupt.annotation.config.SerializeBy;
import xyz.erupt.annotation.config.ToMap;
import xyz.erupt.annotation.fun.ConditionHandler;
import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.EditTypeMapping;
import xyz.erupt.core.annotation.EruptDataProcessor;
import xyz.erupt.core.service.DataService;
import xyz.erupt.core.service.data_impl.DBService;

import java.beans.Transient;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Created by liyuepeng on 2019-02-28.
 */
public class AnnotationUtil {

    private static final String[] ANNOTATION_NUMBER_TYPE = {"short", "int", "long", "float", "double"};

    private static final String[] ANNOTATION_STRING_TYPE = {"String", "byte", "char"};

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
            annotationToJson(annotation, jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private static void annotationToJson(Annotation annotation, JsonObject jsonObject) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        for (Method method : annotation.annotationType().getDeclaredMethods()) {
            Transient tran = method.getAnnotation(Transient.class);
            if (null != tran && tran.value()) continue;
            SerializeBy serializeBy = method.getAnnotation(SerializeBy.class);
            if (null != serializeBy) {
                String type = annotation.getClass().getMethod(serializeBy.method()).invoke(annotation).toString();
                if (!serializeBy.value().equals(type)) {
                    continue;
                }
            }
            String returnType = method.getReturnType().getSimpleName();
            Object result = method.invoke(annotation);
//            NotBlank notBlank = method.getAnnotation(NotBlank.class);
//            if (null != notBlank && notBlank.value()) {
//                if (StringUtils.isBlank(result.toString())) {
//                    continue;
//                }
//            }
            if (returnType.endsWith("[]")) {
                returnType = returnType.substring(0, returnType.length() - 2);
                JsonArray jsonArray = new JsonArray();
                ToMap toMap = method.getAnnotation(ToMap.class);
                JsonObject jsonMap = new JsonObject();
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
                            jsonArray.add((Boolean) res);
                        } else if ("Class".equals(returnType)) {
                            break;
                        } else if (res.getClass().isEnum()) {
                            jsonArray.add(res.toString());
                        } else {
                            Annotation ann = (Annotation) res;
                            if (null != toMap) {
                                JsonObject jo = new JsonObject();
                                annotationToJson((Annotation) res, jo);
                                String key = ann.annotationType().getMethod(toMap.key()).invoke(res).toString();
                                jo.remove(toMap.key());
                                jsonMap.add(key, jo);
                            } else {
                                JsonObject subJsonObject = new JsonObject();
                                annotationToJson(ann, subJsonObject);
                                jsonArray.add(subJsonObject);
                            }
                        }
                    }
                }
                if (null == toMap) {
                    jsonObject.add(method.getName(), jsonArray);
                } else {
                    if (jsonMap.size() > 0) {
                        jsonObject.add(method.getName(), jsonMap);
                    }
                }
            } else {
                if (Arrays.asList(ANNOTATION_STRING_TYPE).contains(returnType)) {
                    jsonObject.addProperty(method.getName(), result.toString());
                } else if (Arrays.asList(ANNOTATION_NUMBER_TYPE).contains(returnType)) {
                    jsonObject.addProperty(method.getName(), (Number) result);
                } else if ("Class".equals(returnType)) {
                    continue;
                } else if ("boolean".equals(returnType)) {
                    jsonObject.addProperty(method.getName(), (Boolean) result);
                } else if (method.getReturnType().isEnum()) {
                    jsonObject.addProperty(method.getName(), result.toString());
                } else if (method.getReturnType().isAnnotation()) {
                    JsonObject subJsonObject = new JsonObject();
                    annotationToJson((Annotation) result, subJsonObject);
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

    public static EditTypeMapping getEditTypeMapping(EditType editType) {
        try {
            return EditType.class.getDeclaredField(editType.name()).getAnnotation(EditTypeMapping.class);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
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

}
