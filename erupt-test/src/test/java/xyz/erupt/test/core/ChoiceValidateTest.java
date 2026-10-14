package xyz.erupt.test.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.context.MetaUser;
import xyz.erupt.core.exception.EruptApiErrorTip;
import xyz.erupt.core.prop.EruptProp;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.service.EruptModifyService;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.test.EruptApplicationTests;
import xyz.erupt.test.model.edit.ChoiceModel;
import xyz.erupt.test.model.edit.MultiChoiceModel;

import static org.junit.jupiter.api.Assertions.*;

/**
 * erupt.choice-validate: writes whose choice values are not among the options are refused.
 */
@Transactional
class ChoiceValidateTest extends EruptApplicationTests {

    @Resource
    private EruptProp eruptProp;

    @Resource
    private EruptModifyService eruptModifyService;

    @BeforeEach
    void on() {
        MetaContext.register(new MetaUser(1L, "admin", "admin"));
        eruptProp.setChoiceValidate(true);
    }

    @AfterEach
    void off() {
        eruptProp.setChoiceValidate(false);
        MetaContext.remove();
    }

    private static EruptModel model(Class<?> clazz) {
        return EruptCoreService.getErupt(clazz.getSimpleName());
    }

    private static JsonObject choice(String status) {
        JsonObject data = new JsonObject();
        data.addProperty("status", status);
        return data;
    }

    @Test
    void offByDefault() {
        eruptProp.setChoiceValidate(false);
        eruptModifyService.insertEruptData(model(ChoiceModel.class), choice("9"));
    }

    @Test
    void aChoiceOutsideTheOptionsIsRefused() {
        eruptModifyService.insertEruptData(model(ChoiceModel.class), choice("1"));
        assertThrows(EruptApiErrorTip.class, () -> eruptModifyService.insertEruptData(model(ChoiceModel.class), choice("9")));
    }

    @Test
    void handlerOptionsAndDisabledOptions() {
        JsonObject fetched = new JsonObject();
        fetched.addProperty("category", "TECH");
        eruptModifyService.insertEruptData(model(ChoiceModel.class), fetched);
        fetched.addProperty("category", "SPACE");
        assertThrows(EruptApiErrorTip.class, () -> eruptModifyService.insertEruptData(model(ChoiceModel.class), fetched), "fetchHandler options");

        JsonObject disabled = new JsonObject();
        disabled.addProperty("level", "C");
        assertThrows(EruptApiErrorTip.class, () -> eruptModifyService.insertEruptData(model(ChoiceModel.class), disabled), "a disabled option is not offered");
    }

    @Test
    void everyMultiChoiceValueIsChecked() {
        JsonObject data = new JsonObject();
        JsonArray roles = new JsonArray();
        roles.add("ADMIN");
        data.add("roles", roles);
        eruptModifyService.insertEruptData(model(MultiChoiceModel.class), data);
        roles.add("ROOT");
        assertThrows(EruptApiErrorTip.class, () -> eruptModifyService.insertEruptData(model(MultiChoiceModel.class), data));
    }

    @Test
    void anUnchangedRetiredValueDoesNotBlockAnEdit() {
        // stored before the option was retired
        ChoiceModel old = new ChoiceModel();
        old.setStatus("9");
        old.setPriority("LOW");
        eruptDao.persist(old);
        eruptDao.flush();

        JsonObject edit = choice("9");
        edit.addProperty("id", old.getId());
        edit.addProperty("priority", "HIGH");
        eruptModifyService.updateEruptData(model(ChoiceModel.class), edit);
        assertEquals("HIGH", eruptDao.find(ChoiceModel.class, old.getId()).getPriority());

        JsonObject moved = choice("8");
        moved.addProperty("id", old.getId());
        assertThrows(EruptApiErrorTip.class, () -> eruptModifyService.updateEruptData(model(ChoiceModel.class), moved), "a new value outside the options");
    }

}
