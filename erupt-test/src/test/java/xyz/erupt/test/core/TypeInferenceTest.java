package xyz.erupt.test.core;

import org.junit.jupiter.api.Test;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.DateType;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.test.EruptApplicationTests;
import xyz.erupt.test.model.edit.TypeInferenceModel;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The Java type of a field is the first source of truth for how it is edited and shown.
 */
public class TypeInferenceTest extends EruptApplicationTests {

    private EruptModel model() {
        return EruptCoreService.getErupt(TypeInferenceModel.class.getSimpleName());
    }

    private EruptFieldModel field(String name) {
        return model().getEruptFieldMap().get(name);
    }

    private EditType editType(String name) {
        return field(name).getEruptField().edit().type();
    }

    private DateType.Type dateType(String name) {
        return field(name).getEruptField().edit().dateType().type();
    }

    private ViewType viewType(String name) {
        return field(name).getEruptField().views()[0].type();
    }

    @Test
    public void dateTypeTest() {
        for (String name : new String[]{"created", "day", "clock", "birthday", "stamp"}) assertEquals(EditType.DATE, editType(name), name);
        assertEquals(DateType.Type.DATE_TIME, dateType("created"));
        assertEquals(DateType.Type.DATE, dateType("day"));
        assertEquals(DateType.Type.TIME, dateType("clock"));
        assertEquals(DateType.Type.DATE, dateType("birthday"));
        assertEquals(DateType.Type.DATE_TIME, dateType("stamp"));
        assertEquals(ViewType.DATE_TIME, viewType("created"));
        assertEquals(ViewType.DATE, viewType("day"));
        assertEquals(ViewType.DATE, viewType("birthday"));
        //the client never sees AUTO: the serialized view carries the resolved type
        EruptFieldModel created = EruptCoreService.getEruptView(TypeInferenceModel.class.getSimpleName()).getEruptFieldMap().get("created");
        assertEquals(DateType.Type.DATE_TIME.name(), created.getEruptFieldJson().getAsJsonObject("edit").getAsJsonObject("dateType").get("type").getAsString());
    }

    @Test
    public void referenceTypeTest() {
        assertEquals(EditType.REFERENCE_TABLE, editType("kind"));
        assertEquals(EditType.REFERENCE_TREE, editType("node"));
        assertEquals("EnumColumnModel", field("kind").getFieldReturnName());
    }

    @Test
    public void collectionTypeTest() {
        assertEquals(EditType.TAB_TABLE_ADD, editType("items"));
        assertEquals(EditType.TAB_TABLE_REFER, editType("links"));
        //the tab components need the element type, which the constructor only knows once AUTO is resolved
        assertEquals("EnumColumnModel", field("items").getFieldReturnName());
        assertEquals("TreeModel", field("links").getFieldReturnName());
        assertEquals(EditType.MULTI_CHOICE, editType("kinds"));
        assertEquals(2, EruptUtil.getChoiceList(model(), field("kinds")).size());
        assertEquals(EditType.KEY_VALUE, editType("attrs"));
        assertEquals(ViewType.TAB_VIEW, viewType("items"));
        assertEquals(ViewType.KEY_VALUE, viewType("attrs"));
    }

}
