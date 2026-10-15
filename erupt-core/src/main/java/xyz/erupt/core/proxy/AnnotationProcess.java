package xyz.erupt.core.proxy;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.SneakyThrows;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import xyz.erupt.annotation.config.EruptProperty;
import xyz.erupt.annotation.config.Match;
import xyz.erupt.annotation.config.ToMap;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.EditTypeMapping;
import xyz.erupt.annotation.sub_field.EditTypeSearch;
import xyz.erupt.core.util.TypeUtil;

import java.beans.Transient;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author YuePeng
 * date 2019-02-28.
 */
public class AnnotationProcess {

    private static final String[] ANNOTATION_NUMBER_TYPE = {"short", "int", "long", "float", "double"};

    private static final String[] ANNOTATION_STRING_TYPE = {"String", "byte", "char"};

    private static final String EMPTY_ARRAY = "[]";

    private static final String VALUE_VAR = "value";

    private static final String ITEM_VAR = "item";

    private static final ExpressionParser parser = new SpelExpressionParser();

    // @Match expressions are annotation literals: a handful of distinct strings parsed on every serialization otherwise
    private static final Map<String, Expression> MATCH_EXPRESSIONS = new ConcurrentHashMap<>();

    @SneakyThrows
    public static JsonObject annotationToJsonByReflect(Annotation annotation) {
        JsonObject jsonObject = new JsonObject();
        for (Method method : annotation.annotationType().getDeclaredMethods()) {
            Transient tran = method.getAnnotation(Transient.class);
            if (null != tran && tran.value()) continue;
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
                Object r = MATCH_EXPRESSIONS.computeIfAbsent(match.value(), parser::parseExpression).getValue(evaluationContext);
                if (null == r || !(Boolean) r) continue;
            }
            if (returnType.endsWith(EMPTY_ARRAY)) {
                returnType = returnType.substring(0, returnType.length() - 2);
                JsonArray jsonArray = new JsonArray();
                ToMap toMap = method.getAnnotation(ToMap.class);
                JsonObject jsonMap = new JsonObject();
                // Primitive types cannot be directly cast to Object arrays, so the following method is used instead
                if (Arrays.asList(ANNOTATION_NUMBER_TYPE).contains(returnType)) {
                    TypeUtil.simpleNumberTypeArrayToObject(result, returnType, jsonArray::add);
                } else {
                    for (Object res : (Object[]) result) {
                        if (String.class.getSimpleName().equals(returnType)) {
                            jsonArray.add(res.toString());
                        } else if (char.class.getSimpleName().equals(returnType)) {
                            jsonArray.add((Character) res);
                        } else if (byte.class.getSimpleName().equals(returnType)) {
                            jsonArray.add((Character) res);
                        } else if (boolean.class.getSimpleName().equals(returnType)) {
                            jsonArray.add((Boolean) res);
                        } else if (Class.class.getSimpleName().equals(returnType)) {
                            jsonArray.add(((Class<?>) res).getSimpleName());
                        } else if (res.getClass().isEnum()) {
                            jsonArray.add(res.toString());
                        } else {
                            Annotation ann = (Annotation) res;
                            if (null != toMap) {
                                JsonObject jo = annotationToJsonByReflect((Annotation) res);
                                String key = ann.annotationType().getMethod(toMap.key()).invoke(res).toString();
                                jo.remove(toMap.key());
                                jsonMap.add(key, jo);
                            } else {
                                jsonArray.add(annotationToJsonByReflect(ann));
                            }
                        }
                    }
                }
                if (null == toMap) {
                    jsonObject.add(methodName, jsonArray);
                } else {
                    if (!jsonMap.isEmpty()) jsonObject.add(methodName, jsonMap);
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
                    jsonObject.add(methodName, annotationToJsonByReflect((Annotation) result));
                } else if (Class.class.getSimpleName().equals(returnType)) {
                    jsonObject.addProperty(methodName, ((Class<?>) result).getSimpleName());
                }
            }
        }
        return jsonObject;
    }

    /**
     * The reverse of {@link #annotationToJsonByReflect}: an annotation read back from its JSON, for a model that
     * exists only as JSON (an erupt-cloud node's build model). A key the JSON does not carry (a {@code @Transient}
     * attribute, a {@code @Match} branch not taken) answers the declared default, and so does a {@code Class}
     * attribute, whose simple name cannot be loaded back. A key present with a value of the wrong shape fails,
     * naming it: a reader such as a validator must not quietly fall back to the more permissive default.
     *
     * @throws AnnotationJsonException when an attribute is read whose JSON does not fit its type
     */
    @SuppressWarnings("unchecked")
    public static <A extends Annotation> A jsonToAnnotation(Class<A> type, JsonObject json) {
        return (A) Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type}, (proxy, method, args) -> switch (method.getName()) {
            case "annotationType" -> type;
            case "toString" -> "@" + type.getName() + json;
            case "hashCode" -> System.identityHashCode(proxy);
            case "equals" -> proxy == args[0];
            default -> {
                EruptProperty property = method.getAnnotation(EruptProperty.class);
                String key = null != property && !property.alias().isEmpty() ? property.alias() : method.getName();
                JsonElement value = null == json ? null : json.get(key);
                if (null == value || value.isJsonNull() || isClass(method.getReturnType())) yield method.getDefaultValue();
                yield fromJson(method.getReturnType(), value, type.getSimpleName() + "." + key);
            }
        });
    }

    /**
     * The JSON of an annotation as {@link #annotationToJsonByReflect} would write it, completed from the given JSON:
     * every attribute it leaves out gets its default (nested annotations and annotation arrays included), every
     * attribute it carries is kept as it is. A JSON that does not read back as the annotation is returned unchanged.
     */
    public static <A extends Annotation> JsonObject completeJson(Class<A> type, JsonObject json) {
        try {
            JsonObject complete = json.deepCopy();
            fillDefaults(complete, annotationToJsonByReflect(jsonToAnnotation(type, json)));
            return complete;
        } catch (RuntimeException e) {
            return json;
        }
    }

    private static void fillDefaults(JsonObject target, JsonObject defaults) {
        for (Map.Entry<String, JsonElement> entry : defaults.entrySet()) {
            JsonElement own = target.get(entry.getKey());
            JsonElement value = entry.getValue();
            if (null == own || own.isJsonNull()) {
                target.add(entry.getKey(), value);
            } else if (own.isJsonObject() && value.isJsonObject()) {
                fillDefaults(own.getAsJsonObject(), value.getAsJsonObject());
            } else if (own.isJsonArray() && value.isJsonArray() && own.getAsJsonArray().size() == value.getAsJsonArray().size()) {
                for (int i = 0; i < own.getAsJsonArray().size(); i++) {
                    if (own.getAsJsonArray().get(i).isJsonObject() && value.getAsJsonArray().get(i).isJsonObject()) {
                        fillDefaults(own.getAsJsonArray().get(i).getAsJsonObject(), value.getAsJsonArray().get(i).getAsJsonObject());
                    }
                }
            }
        }
    }

    private static boolean isClass(Class<?> type) {
        return type == Class.class || (type.isArray() && type.getComponentType() == Class.class);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object fromJson(Class<?> type, JsonElement value, String path) {
        try {
            if (type.isArray()) {
                if (!value.isJsonArray()) throw new IllegalStateException("not an array");
                JsonArray items = value.getAsJsonArray();
                Object array = Array.newInstance(type.getComponentType(), items.size());
                for (int i = 0; i < items.size(); i++) {
                    Array.set(array, i, fromJson(type.getComponentType(), items.get(i), path + "[" + i + "]"));
                }
                return array;
            }
            if (type.isAnnotation()) {
                if (!value.isJsonObject()) throw new IllegalStateException("not an object");
                return jsonToAnnotation((Class<Annotation>) type, value.getAsJsonObject());
            }
            if (!value.isJsonPrimitive()) throw new IllegalStateException("not a value");
            if (type.isEnum()) return Enum.valueOf((Class<Enum>) type, value.getAsString());
            if (type == String.class) return value.getAsString();
            if (type == boolean.class) {
                if (!value.getAsJsonPrimitive().isBoolean()) throw new IllegalStateException("not a boolean");
                return value.getAsBoolean();
            }
            if (type == char.class) {
                if (value.getAsString().length() != 1) throw new IllegalStateException("not one character");
                return value.getAsString().charAt(0);
            }
            BigDecimal number = new BigDecimal(value.getAsString());
            if (type == int.class) return number.intValueExact();
            if (type == long.class) return number.longValueExact();
            if (type == short.class) return number.shortValueExact();
            if (type == byte.class) return number.byteValueExact();
            if (type == double.class) return number.doubleValue();
            if (type == float.class) return number.floatValue();
            throw new IllegalStateException("unsupported type " + type.getSimpleName());
        } catch (AnnotationJsonException e) {
            throw e; // already names the innermost attribute
        } catch (RuntimeException e) {
            throw new AnnotationJsonException(path + " = " + value + ": " + e.getMessage(), e);
        }
    }

    // A value of the wrong shape in annotation JSON, naming the attribute path
    public static final class AnnotationJsonException extends IllegalArgumentException {
        AnnotationJsonException(String message, Throwable cause) {
            super("annotation JSON " + message, cause);
        }
    }

    // Original serialization implementation of "erupt annotation", collection ✨
    @Deprecated
    public static String annotationToJsonByReplace(String annotationStr) throws JSONException {
        String convertStr = annotationStr
                .replaceAll("@xyz\\.erupt\\.annotation\\.sub_field\\.sub_edit\\.sub_attachment\\.\\w+", "")
                .replaceAll("@xyz\\.erupt\\.annotation\\.sub_field\\.sub_edit\\.\\w+", "")
                .replaceAll("@xyz\\.erupt\\.annotation\\.sub_field\\.sub_view\\.\\w+", "")
                .replaceAll("@xyz\\.erupt\\.annotation\\.sub_field\\.\\w+", "")
                .replaceAll("@xyz\\.erupt\\.annotation\\.sub_erupt\\.\\w+", "")
                .replaceAll("@xyz\\.erupt\\.annotation\\.\\w+", "")
                // Blocked information
                .replaceAll("class [a-zA-Z0-9.]+", "")
                .replace("=,", "='',")
                .replace("=)", "='')")
                .replace("=", ":")
                .replace("(", "{")
                .replace(")", "}");
        return new JSONObject(convertStr).toString();
    }

    @SneakyThrows
    public static EditTypeMapping getEditTypeMapping(EditType editType) {
        return EditType.class.getDeclaredField(editType.name()).getAnnotation(EditTypeMapping.class);
    }

    @SneakyThrows
    public static EditTypeSearch getEditTypeSearch(EditType editType) {
        return EditType.class.getDeclaredField(editType.name()).getAnnotation(EditTypeSearch.class);
    }

}
