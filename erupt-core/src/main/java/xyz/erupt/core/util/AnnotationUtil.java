package xyz.erupt.core.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.json.JSONObject;
import xyz.erupt.annotation.config.EruptProperty;
import xyz.erupt.annotation.config.SerializeBy;
import xyz.erupt.annotation.config.ToMap;
import xyz.erupt.annotation.config.Volatile;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.constant.JavaType;
import xyz.erupt.annotation.fun.FilterHandler;
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
 * @author liyuepeng
 * @date 2019-02-28.
 */
public class AnnotationUtil {

    private static final String[] ANNOTATION_NUMBER_TYPE = {"short", "int", "long", "float", "double"};

    private static final String[] ANNOTATION_STRING_TYPE = {"String", "byte", "char"};

    private static final String EMPTY_ARRAY = "[]";

    @Deprecated
    public static String annotationToJsonByReplace(String annotationStr) {
        String convertStr = annotationStr
                .replaceAll("@xyz\\.erupt\\.annotation\\.sub_field\\.sub_edit\\.sub_attachment\\.\\w+", "")
                .replaceAll("@xyz\\.erupt\\.annotation\\.sub_field\\.sub_edit\\.\\w+", "")
                .replaceAll("@xyz\\.erupt\\.annotation\\.sub_field\\.sub_view\\.\\w+", "")
                .replaceAll("@xyz\\.erupt\\.annotation\\.sub_field\\.\\w+", "")
                .replaceAll("@xyz\\.erupt\\.annotation\\.sub_erupt\\.\\w+", "")
                .replaceAll("@xyz\\.erupt\\.annotation\\.\\w+", "")
                //屏蔽类信息
                .replaceAll("class [a-zA-Z0-9.]+", "")
                .replace("=,", "='',")
                .replace("=)", "='')")
                .replace("=", ":")
                .replace("(", "{")
                .replace(")", "}");
        return new JSONObject(convertStr).toString();
    }

    public static JsonObject annotationToJsonByReflect(Annotation annotation) {
        try {
            return annotationToJson(annotation);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static JsonObject annotationToJson(Annotation annotation)
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        JsonObject jsonObject = new JsonObject();
//        NotBlank notBlank = annotation.annotationType().getAnnotation(NotBlank.class);
//        if (null != notBlank) {
//            String val = (String) annotation.annotationType().getDeclaredMethod(notBlank.value()).invoke(annotation);
//            if (StringUtils.isBlank(val)) {
//                return null;
//            }
//        }
        for (Method method : annotation.annotationType().getDeclaredMethods()) {
            Transient tran = method.getAnnotation(Transient.class);
            if (null != tran && tran.value()) {
                continue;
            }
            SerializeBy serializeBy = method.getAnnotation(SerializeBy.class);
            if (null != serializeBy) {
                String type = annotation.getClass().getMethod(serializeBy.method()).invoke(annotation).toString();
                if (!serializeBy.value().equals(type)) {
                    continue;
                }
            }
            String methodName = method.getName();
            EruptProperty eruptProperty = method.getAnnotation(EruptProperty.class);
            if (null != eruptProperty && !AnnotationConst.EMPTY_STR.equals(eruptProperty.alias())) {
                methodName = eruptProperty.alias();
            }
            String returnType = method.getReturnType().getSimpleName();
            Object result = method.invoke(annotation);
            Volatile vol = method.getAnnotation(Volatile.class);
            if (null != vol) {
                jsonObject.add(methodName, vol.value().newInstance().exec(method.invoke(annotation)));
            } else if (returnType.endsWith(EMPTY_ARRAY)) {
                returnType = returnType.substring(0, returnType.length() - 2);
                JsonArray jsonArray = new JsonArray();
                ToMap toMap = method.getAnnotation(ToMap.class);
                JsonObject jsonMap = new JsonObject();
                //基本类型无法强转成Object类型数组，所以使用下面的方法进行处理
                if (Arrays.asList(ANNOTATION_NUMBER_TYPE).contains(returnType)) {
                    TypeUtil.simpleNumberTypeArrayToObject(result, returnType, (number) -> jsonArray.add(number));
                } else if (JavaType.CHAR.equals(returnType)) {
                    char[] intArray = (char[]) result;
                    for (char i : intArray) {
                        jsonArray.add(i);
                    }
                } else if (JavaType.BYTE.equals(returnType)) {
                    byte[] intArray = (byte[]) result;
                    for (byte i : intArray) {
                        jsonArray.add(i);
                    }
                } else {
                    Object[] resultArray = (Object[]) result;
                    for (Object res : resultArray) {
                        if (JavaType.STRING.equals(returnType)) {
                            jsonArray.add(res.toString());
                        } else if (JavaType.BOOLEAN.equals(returnType)) {
                            jsonArray.add((Boolean) res);
                        } else if (JavaType.CLASS.equals(returnType)) {
                            break;
                        } else if (res.getClass().isEnum()) {
                            jsonArray.add(res.toString());
                        } else {
                            Annotation ann = (Annotation) res;
                            if (null != toMap) {
                                JsonObject jo = annotationToJson((Annotation) res);
                                String key = ann.annotationType().getMethod(toMap.key()).invoke(res).toString();
                                jo.remove(toMap.key());
                                jsonMap.add(key, jo);
                            } else {
                                jsonArray.add(annotationToJson(ann));
                            }
                        }
                    }
                }
                if (null == toMap) {
                    jsonObject.add(methodName, jsonArray);
                } else {
                    if (jsonMap.size() > 0) {
                        jsonObject.add(methodName, jsonMap);
                    }
                }
            } else {
                if (Arrays.asList(ANNOTATION_STRING_TYPE).contains(returnType)) {
                    jsonObject.addProperty(methodName, result.toString());
                } else if (Arrays.asList(ANNOTATION_NUMBER_TYPE).contains(returnType)) {
                    jsonObject.addProperty(methodName, (Number) result);
                } else if (JavaType.BOOLEAN.equals(returnType)) {
                    jsonObject.addProperty(methodName, (Boolean) result);
                } else if (method.getReturnType().isEnum()) {
                    jsonObject.addProperty(methodName, result.toString());
                } else if (method.getReturnType().isAnnotation()) {
                    jsonObject.add(methodName, annotationToJson((Annotation) result));
                } else if (JavaType.CLASS.equals(returnType)) {
                    continue;
                }
            }
        }
        return jsonObject;
    }

    public static String switchFilterConditionToStr(Filter filter) {
        String condition = filter.condition();
        for (Class<? extends FilterHandler> conditionHandler : filter.conditionHandlers()) {
            FilterHandler ch = EruptSpringUtil.getBean(conditionHandler);
            condition = ch.filter(condition, filter.params());
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
            dataService = EruptSpringUtil.getBean(eruptDataProcessor.processors());
        } else {
            dataService = EruptSpringUtil.getBean(DBService.class);
        }
        return dataService;
    }

}
