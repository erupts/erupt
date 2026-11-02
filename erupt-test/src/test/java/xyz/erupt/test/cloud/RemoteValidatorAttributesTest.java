package xyz.erupt.test.cloud;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import org.junit.jupiter.api.Test;
import xyz.erupt.annotation.config.EruptProperty;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.core.exception.EruptApiErrorTip;
import xyz.erupt.core.proxy.AnnotationProcess;
import xyz.erupt.core.util.EruptValueValidator;
import xyz.erupt.test.EruptApplicationTests;

import java.beans.Transient;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A remote erupt reaches {@link EruptValueValidator} as its build JSON read back through
 * {@link AnnotationProcess#jsonToAnnotation}, which can only answer what the JSON carries. Every attribute the
 * validator reads must therefore be serialized: not {@code @Transient}, and present in the annotation schema.
 * The attributes are recorded while the validator runs every rule, so a new rule is covered without listing it.
 */
public class RemoteValidatorAttributesTest extends EruptApplicationTests {

    private static final Path SCHEMA = Path.of("..", "schema", "erupt-annotations.schema.json");

    // every limit set, so each rule goes past its "nothing declared" shortcut
    private static final String EDIT = """
            {"title": "F", "type": "%s", "inputType": {"length": 3},
             "numberType": {"min": 0, "max": 10}, "sliderType": {"min": 0, "max": 10},
             "rateType": {"count": 5, "allowHalf": false},
             "tagsType": {"maxTagCount": 2, "allowExtension": false, "joinSeparator": "[]"},
             "dateType": {"type": "DATE", "pickerMode": "FUTURE", "min": "2020-01-01", "max": "2099-01-01"},
             "choiceType": {"validate": true, "dependField": ""}, "multiChoiceType": {"validate": true, "dependField": ""}}""";

    @Test
    void theValidatorReadsOnlySerializedAttributes() throws Exception {
        Set<Method> read = new LinkedHashSet<>();
        Object[][] runs = {
                {EditType.CHOICE, "b", "a"},
                {EditType.MULTI_CHOICE, array("b"), array("a")},
                {EditType.INPUT, "long", "ok"},
                {EditType.NUMBER, "11", "5"},
                {EditType.SLIDER, "11", "5"},
                {EditType.RATE, "2.5", "2"},
                {EditType.TAGS, "[\"a\",\"b\",\"c\"]", "[\"a\",\"z\"]"},
                {EditType.TAGS, "[\"a\"]", "[\"a\"]"},
                {EditType.DATE, "2000-01-01", "2098-01-01"},
        };
        for (Object[] run : runs) {
            Edit edit = recording(AnnotationProcess.jsonToAnnotation(Edit.class,
                    JsonParser.parseString(EDIT.formatted(run[0])).getAsJsonObject()), read);
            for (Object value : new Object[]{run[1], run[2]}) {
                JsonElement json = value instanceof JsonElement element ? element : new JsonPrimitive((String) value);
                try {
                    EruptValueValidator.validate(edit, json, Collections.emptyList(), () -> Set.of("a"));
                } catch (EruptApiErrorTip refused) {
                    // refusals are expected: the point is which attributes were read on the way
                }
            }
        }
        JsonObject defs = JsonParser.parseString(Files.readString(SCHEMA, StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject("$defs");
        List<String> unserialized = new ArrayList<>();
        for (Method method : read) {
            Transient tran = method.getAnnotation(Transient.class);
            EruptProperty property = method.getAnnotation(EruptProperty.class);
            String key = null != property && !property.alias().isEmpty() ? property.alias() : method.getName();
            JsonObject def = defs.getAsJsonObject(method.getDeclaringClass().getSimpleName());
            if ((null != tran && tran.value()) || null == def || !def.getAsJsonObject("properties").has(key)) {
                unserialized.add(method.getDeclaringClass().getSimpleName() + "." + key);
            }
        }
        assertTrue(read.stream().anyMatch(m -> m.getName().equals("allowHalf")), "the recording saw the rules run: " + read);
        assertEquals(List.of(), unserialized, "read by EruptValueValidator but not in a build model's JSON");
    }

    private static JsonArray array(String value) {
        JsonArray array = new JsonArray();
        array.add(value);
        return array;
    }

    // The annotation, recording each attribute read, nested annotations included
    @SuppressWarnings("unchecked")
    private static <A extends Annotation> A recording(A target, Set<Method> read) {
        Class<? extends Annotation> type = target.annotationType();
        return (A) Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type}, (proxy, method, args) -> {
            if (method.getDeclaringClass() == type) read.add(method);
            Object result;
            try {
                result = method.invoke(target, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
            return result instanceof Annotation nested && method.getDeclaringClass() == type ? recording(nested, read) : result;
        });
    }

}
