package xyz.erupt.designer;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.core.view.EruptBuildModel;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.designer.pojo.DesignerForm;
import xyz.erupt.designer.service.EruptDesignerService;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author YuePeng
 * date 2026-06-12
 */
public class EruptDesignerServiceTest {

    private final EruptDesignerService service = new EruptDesignerService();

    private final Gson gson = new Gson();

    private DesignerForm.DesignerField field(String fieldName, String viewJson, String editJson) {
        DesignerForm.DesignerField field = new DesignerForm.DesignerField();
        field.setFieldName(fieldName);
        if (null != viewJson) field.setView(gson.fromJson(viewJson, JsonObject.class));
        if (null != editJson) field.setEdit(gson.fromJson(editJson, JsonObject.class));
        return field;
    }

    @Test
    public void disguisedAnnotation() throws Exception {
        DesignerForm form = new DesignerForm();
        form.setClassName("Goods");
        form.setErupt(gson.fromJson("{name:'Product Management', power:{export:true, delete:false}}", JsonObject.class));
        form.setFields(Arrays.asList(
                field("name", "{title:'Product Name', sortable:true}",
                        "{title:'Product Name', notNull:true, type:'INPUT', inputType:{length:100}, search:{value:true}}"),
                field("price", "{title:'Price'}", "{title:'Price', type:'NUMBER', numberType:{min:0}}"),
                field("type", "{title:'Category'}",
                        "{title:'Category', type:'CHOICE', choiceType:{vl:[{value:'1',label:'Electronics'},{value:'2',label:'Digital'}]}}")
        ));

        EruptModel model = service.toEruptModel(form);

        // class-level disguised @Erupt
        assertEquals("Goods", model.getEruptName());
        assertEquals("Product Management", model.getErupt().name());
        assertTrue(model.getErupt().power().export());
        assertFalse(model.getErupt().power().delete());
        assertTrue(model.getErupt().power().add()); // untouched member falls back to template default

        // field-level disguised @EruptField
        EruptFieldModel name = model.getEruptFieldMap().get("name");
        Edit edit = name.getEruptField().edit();
        assertEquals("Product Name", edit.title());
        assertTrue(edit.notNull());
        assertEquals(EditType.INPUT, edit.type());
        assertEquals(100, edit.inputType().length());
        assertTrue(edit.search().value());
        assertEquals("Product Name", name.getEruptField().views()[0].title());
        assertTrue(name.getEruptField().views()[0].sortable());

        assertEquals(EruptFieldModel.NUMBER, model.getEruptFieldMap().get("price").getFieldReturnName());
        assertEquals(0, model.getEruptFieldMap().get("price").getEruptField().edit().numberType().min());

        // annotation array expanded from template prototype
        EruptFieldModel type = model.getEruptFieldMap().get("type");
        assertEquals(2, type.getEruptField().edit().choiceType().vl().length);
        assertEquals("Electronics", type.getEruptField().edit().choiceType().vl()[0].label());

        // the disguised model serializes through the standard pipeline (what the frontend consumes)
        EruptBuildModel buildModel = service.preview(form);
        EruptModel cloned = buildModel.getEruptModel();
        assertEquals("Goods", cloned.getEruptName());
        // @Transient members (name/power...) never serialize into eruptJson — power flows via EruptBuildModel
        assertTrue(buildModel.getPower().isExport());
        assertFalse(buildModel.getPower().isDelete());
        JsonObject nameJson = cloned.getEruptFieldModels().stream()
                .filter(it -> "name".equals(it.getFieldName())).findFirst().orElseThrow().getEruptFieldJson();
        assertEquals("Product Name", nameJson.getAsJsonObject("edit").get("title").getAsString());
        assertEquals(100, nameJson.getAsJsonObject("edit").getAsJsonObject("inputType").get("length").getAsInt());
        // CHOICE options resolved into componentValue for the standard frontend choice component
        EruptFieldModel typeField = cloned.getEruptFieldModels().stream()
                .filter(it -> "type".equals(it.getFieldName())).findFirst().orElseThrow();
        assertEquals(2, ((java.util.List<?>) typeField.getComponentValue()).size());
    }

}
