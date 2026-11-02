package xyz.erupt.test.cloud;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.sub_edit.DateType;
import xyz.erupt.core.proxy.AnnotationProcess;
import xyz.erupt.core.proxy.AnnotationProcess.AnnotationJsonException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JsonAnnotationTest {

    private static Edit edit(String json) {
        return AnnotationProcess.jsonToAnnotation(Edit.class, JsonParser.parseString(json).getAsJsonObject());
    }

    @Test
    void readsJsonBackAsTheAnnotation() {
        Edit edit = edit("""
                {"title": "Day", "type": "DATE", "notNull": true, "readOnly": {"edit": false},
                 "dateType": {"type": "DATE", "pickerMode": "FUTURE", "max": "2030-01-01"},
                 "numberType": {"min": 3, "max": 9}, "search": {"value": true}, "onchange": "OnChange"}""");
        assertEquals("Day", edit.title());
        assertEquals(EditType.DATE, edit.type());
        assertTrue(edit.notNull());
        assertFalse(edit.readonly().edit(), "@EruptProperty alias readOnly");
        assertTrue(edit.readonly().add(), "absent nested attribute: its default");
        assertEquals(DateType.PickerMode.FUTURE, edit.dateType().pickerMode());
        assertEquals("2030-01-01", edit.dateType().max());
        assertEquals(3, edit.numberType().min());
        assertEquals(255, edit.inputType().length(), "absent nested annotation: its default");
        assertTrue(edit.search().value());
        assertArrayEquals(new String[0], edit.onchangeParams(), "absent array: its default");
        assertEquals(Edit.class, edit.annotationType());
        // a Class attribute carries only a simple name, which cannot be loaded back
        assertEquals(edit.onchange(), edit("{}").onchange());
    }

    @Test
    void aValueOfTheWrongShapeFailsNamingIt() {
        assertTrue(assertThrows(AnnotationJsonException.class, () -> edit("{\"numberType\": {\"min\": \"many\"}}").numberType().min())
                .getMessage().contains("NumberType.min"));
        assertThrows(AnnotationJsonException.class, () -> edit("{\"notNull\": \"yes\"}").notNull(), "boolean as text");
        assertThrows(AnnotationJsonException.class, () -> edit("{\"type\": \"SPINNER\"}").type(), "unknown enum constant");
        assertThrows(AnnotationJsonException.class, () -> edit("{\"inputType\": {\"length\": 3000000000}}").inputType().length(), "int overflow");
        assertThrows(AnnotationJsonException.class, () -> edit("{\"inputType\": {\"length\": 2.5}}").inputType().length(), "a fraction for an int");
        assertThrows(AnnotationJsonException.class, () -> edit("{\"numberType\": 5}").numberType(), "a value for an annotation");
        assertThrows(AnnotationJsonException.class, () -> edit("{\"onchangeParams\": \"a\"}").onchangeParams(), "a value for an array");
    }

}
