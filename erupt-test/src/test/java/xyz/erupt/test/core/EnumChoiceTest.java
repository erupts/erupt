package xyz.erupt.test.core;

import org.junit.jupiter.api.Test;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.test.EruptApplicationTests;
import xyz.erupt.test.model.edit.EnumColumnModel;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * An enum field is a dropdown out of the box: AUTO resolves to CHOICE and, with no vl or fetchHandler
 * declared, the options are the enum constants by name.
 */
public class EnumChoiceTest extends EruptApplicationTests {

    @Test
    public void enumFieldIsChoiceTest() {
        EruptModel model = EruptCoreService.getErupt(EnumColumnModel.class.getSimpleName());
        EruptFieldModel plain = model.getEruptFieldMap().get("plain");
        assertEquals(EditType.CHOICE, plain.getEruptField().edit().type());
        List<VLModel> vls = EruptUtil.getChoiceList(model, plain);
        assertEquals(List.of("A", "B"), vls.stream().map(VLModel::getValue).toList());
        assertEquals(List.of("A", "B"), vls.stream().map(VLModel::getLabel).toList());
        //the view carries them to the frontend as the component value, reachable through the cloned map as well
        EruptModel view = EruptCoreService.getEruptView(EnumColumnModel.class.getSimpleName());
        EruptFieldModel viewPlain = view.getEruptFieldMap().get("plain");
        assertSame(viewPlain, view.getEruptFieldModels().stream().filter(f -> "plain".equals(f.getFieldName())).findFirst().orElseThrow());
        assertEquals(2, ((List<?>) viewPlain.getComponentValue()).size());
        //the registered model is untouched
        assertNull(plain.getComponentValue());
    }

}
