package xyz.erupt.designer;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import xyz.erupt.annotation.sub_erupt.Tpl;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.ViewType;
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

    /** HIDDEN / EMPTY / TPL go through the string template field; TPL config is disguised onto @Tpl. */
    @Test
    public void hiddenEmptyAndTplTypes() throws Exception {
        DesignerForm form = new DesignerForm();
        form.setClassName("TplDemo");
        form.setErupt(gson.fromJson("{name:'Tpl Demo'}", JsonObject.class));
        form.setFields(Arrays.asList(
                field("token", "{title:'Token'}", "{title:'Token', type:'HIDDEN'}"),
                field("gap", null, "{title:'Gap', type:'EMPTY'}"),
                field("chart", null, "{title:'Chart', type:'TPL', tplType:{path:'/tpl/chart.html', engine:'FreeMarker', enable:true}}")
        ));

        EruptModel model = service.toEruptModel(form);

        EruptFieldModel token = model.getEruptFieldMap().get("token");
        assertEquals(EditType.HIDDEN, token.getEruptField().edit().type());
        assertEquals(String.class.getSimpleName(), token.getFieldReturnName());
        assertEquals(1, token.getEruptField().views().length);

        // no view json → no table column
        assertEquals(0, model.getEruptFieldMap().get("gap").getEruptField().views().length);

        Tpl tpl = model.getEruptFieldMap().get("chart").getEruptField().edit().tplType();
        assertTrue(tpl.enable());
        assertEquals("/tpl/chart.html", tpl.path());
        assertEquals(Tpl.Engine.FreeMarker, tpl.engine());
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

    /** AUTO never resolves for a designed field, so the view type is settled from the edit type. */
    @Test
    public void viewType() throws Exception {
        DesignerForm form = new DesignerForm();
        form.setClassName("Label");
        form.setErupt(gson.fromJson("{name:'Label'}", JsonObject.class));
        form.setFields(Arrays.asList(
                field("color", "{title:'Color'}", "{title:'Color', type:'COLOR'}"),
                field("createTime", "{title:'Created'}", "{title:'Created', type:'DATE', dateType:{type:'DATE_TIME'}}"),
                field("remark", "{title:'Remark'}", "{title:'Remark', type:'TEXTAREA'}"),
                field("cover", "{title:'Cover', type:'IMAGE'}", "{title:'Cover', type:'ATTACHMENT'}")
        ));

        EruptModel model = service.toEruptModel(form);
        assertEquals(ViewType.COLOR, model.getEruptFieldMap().get("color").getEruptField().views()[0].type());
        assertEquals(ViewType.DATE_TIME, model.getEruptFieldMap().get("createTime").getEruptField().views()[0].type());
        assertEquals(ViewType.TEXT, model.getEruptFieldMap().get("remark").getEruptField().views()[0].type());
        // an explicit view type wins over the derived one
        assertEquals(ViewType.IMAGE, model.getEruptFieldMap().get("cover").getEruptField().views()[0].type());

        // settled on the design itself, so a publish persists it
        assertEquals("COLOR", form.getFields().get(0).getView().get("type").getAsString());
    }

    @Test
    public void textareaType() throws Exception {
        DesignerForm form = new DesignerForm();
        form.setClassName("Note");
        form.setErupt(gson.fromJson("{name:'Note'}", JsonObject.class));
        form.setFields(Arrays.asList(
                field("content", "{title:'Content'}",
                        "{title:'Content', type:'TEXTAREA', textareaType:{length:100}}")
        ));

        Edit edit = service.toEruptModel(form).getEruptFieldMap().get("content").getEruptField().edit();
        assertEquals(EditType.TEXTAREA, edit.type());
        assertEquals(100, edit.textareaType().length());
        assertEquals(3, edit.textareaType().minRows()); // untouched member falls back to default

        // what the frontend consumes: textareaType must survive the @Match filter and serialize
        JsonObject contentJson = service.preview(form).getEruptModel().getEruptFieldModels().stream()
                .filter(it -> "content".equals(it.getFieldName())).findFirst().orElseThrow().getEruptFieldJson();
        assertEquals(100, contentJson.getAsJsonObject("edit").getAsJsonObject("textareaType").get("length").getAsInt());
    }

}
