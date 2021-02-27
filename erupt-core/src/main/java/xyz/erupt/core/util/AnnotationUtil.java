package xyz.erupt.core.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.SneakyThrows;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import xyz.erupt.annotation.config.EruptProperty;
import xyz.erupt.annotation.config.Match;
import xyz.erupt.annotation.config.ToMap;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.expr.Expr;
import xyz.erupt.annotation.expr.ExprBool;
import xyz.erupt.annotation.fun.FilterHandler;
import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.EditTypeMapping;
import xyz.erupt.annotation.sub_field.EditTypeSearch;
import xyz.erupt.core.annotation.EruptDataProcessor;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.service.IEruptDataService;

import javax.script.ScriptException;
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

//    @Deprecated
//    public static String annotationToJsonByReplace(String annotationStr) {
//        String convertStr = annotationStr
//                .replaceAll("@xyz\\.erupt\\.annotation\\.sub_field\\.sub_edit\\.sub_attachment\\.\\w+", "")
//                .replaceAll("@xyz\\.erupt\\.annotation\\.sub_field\\.sub_edit\\.\\w+", "")
//                .replaceAll("@xyz\\.erupt\\.annotation\\.sub_field\\.sub_view\\.\\w+", "")
//                .replaceAll("@xyz\\.erupt\\.annotation\\.sub_field\\.\\w+", "")
//                .replaceAll("@xyz\\.erupt\\.annotation\\.sub_erupt\\.\\w+", "")
//                .replaceAll("@xyz\\.erupt\\.annotation\\.\\w+", "")
//                //屏蔽类信息
//                .replaceAll("class [a-zA-Z0-9.]+", "")
//                .replace("=,", "='',")
//                .replace("=)", "='')")
//                .replace("=", ":")
//                .replace("(", "{")
//                .replace(")", "}");
//        return new JSONObject(convertStr).toString();
//    }

//    private static final ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

    private static final ExpressionParser parser = new SpelExpressionParser();

    @SneakyThrows
    public static JsonObject annotationToJsonByReflect(Annotation annotation) {
        return annotationToJson(annotation);
    }

    private static final String VALUE_VAR = "value";

    private static final String ITEM_VAR = "item";

    //耗时操作，勿频繁调用
    private static JsonObject annotationToJson(Annotation annotation)
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException, ScriptException {
        JsonObject jsonObject = new JsonObject();
        for (Method method : annotation.annotationType().getDeclaredMethods()) {
            Transient tran = method.getAnnotation(Transient.class);
            if (null != tran && tran.value()) {
                continue;
            }
            String methodName = method.getName();
            EruptProperty eruptProperty = method.getAnnotation(EruptProperty.class);
            if (null != eruptProperty && !AnnotationConst.EMPTY_STR.equals(eruptProperty.alias())) {
                methodName = eruptProperty.alias();
            }
            String returnType = method.getReturnType().getSimpleName();
            Object result = method.invoke(annotation);
            Match match = method.getAnnotation(Match.class);
            if (null != match) {
                EvaluationContext evaluationContext = new StandardEvaluationContext();
                evaluationContext.setVariable(VALUE_VAR, result);
                evaluationContext.setVariable(ITEM_VAR, annotation);
                Object r = parser.parseExpression(match.value()).getValue(evaluationContext);
                if (null == r || !(Boolean) r) {
                    continue;
                }
            }
            if (returnType.endsWith(EMPTY_ARRAY)) {
                returnType = returnType.substring(0, returnType.length() - 2);
                JsonArray jsonArray = new JsonArray();
                ToMap toMap = method.getAnnotation(ToMap.class);
                JsonObject jsonMap = new JsonObject();
                //基本类型无法强转成Object类型数组，所以使用下面的方法进行处理
                if (Arrays.asList(ANNOTATION_NUMBER_TYPE).contains(returnType)) {
                    TypeUtil.simpleNumberTypeArrayToObject(result, returnType, jsonArray::add);
                } else if (char.class.getSimpleName().equals(returnType)) {
                    char[] intArray = (char[]) result;
                    for (char i : intArray) {
                        jsonArray.add(i);
                    }
                } else if (byte.class.getSimpleName().equals(returnType)) {
                    byte[] intArray = (byte[]) result;
                    for (byte i : intArray) {
                        jsonArray.add(i);
                    }
                } else {
                    Object[] resultArray = (Object[]) result;
                    for (Object res : resultArray) {
                        if (String.class.getSimpleName().equals(returnType)) {
                            jsonArray.add(res.toString());
                        } else if (boolean.class.getSimpleName().equals(returnType)) {
                            jsonArray.add((Boolean) res);
                        } else if (Class.class.getSimpleName().equals(returnType)) {
                            jsonArray.add(((Class<?>) res).getSimpleName());
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
                } else if (boolean.class.getSimpleName().equals(returnType)) {
                    jsonObject.addProperty(methodName, (Boolean) result);
                } else if (method.getReturnType().isEnum()) {
                    jsonObject.addProperty(methodName, result.toString());
                } else if (method.getReturnType().isAnnotation()) {
                    jsonObject.add(methodName, annotationToJson((Annotation) result));
                } else if (Class.class.getSimpleName().equals(returnType)) {
                    jsonObject.addProperty(methodName, ((Class<?>) result).getSimpleName());
                }
            }
        }
        return jsonObject;
    }

    public static String switchFilterConditionToStr(Filter filter) {
        String condition = filter.value();
        if (!filter.conditionHandler().isInterface()) {
            FilterHandler ch = EruptSpringUtil.getBean(filter.conditionHandler());
            condition = ch.filter(condition, filter.params());
        }
        return condition;
    }

    public static String getExpr(Expr expr) {
        String value = expr.value();
        if (!expr.exprHandler().isInterface()) {
            value = EruptSpringUtil.getBean(expr.exprHandler()).handler(value, expr.params());
        }
        return value;
    }

    public static boolean getExprBool(ExprBool expr) {
        boolean value = expr.value();
        if (!expr.exprHandler().isInterface()) {
            value = EruptSpringUtil.getBean(expr.exprHandler()).boolHandler(value, expr.params());
        }
        return value;
    }

    public static EditTypeMapping getEditTypeMapping(EditType editType) {
        try {
            return EditType.class.getDeclaredField(editType.name()).getAnnotation(EditTypeMapping.class);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static EditTypeSearch getEditTypeSearch(EditType editType) {
        try {
            return EditType.class.getDeclaredField(editType.name()).getAnnotation(EditTypeSearch.class);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static IEruptDataService getEruptDataProcessor(Class<?> clazz) {
        EruptDataProcessor eruptDataProcessor = clazz.getAnnotation(EruptDataProcessor.class);
        if (null == eruptDataProcessor) {
            return EruptSpringUtil.getBean(DataProcessorManager.getDataProcessor(EruptConst.DEFAULT_DATA_PROCESSOR));
        } else {
            return EruptSpringUtil.getBean(DataProcessorManager.getDataProcessor(eruptDataProcessor.value()));
        }
    }


}
