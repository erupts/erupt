package xyz.erupt.ai_canvas.fun;

import org.junit.jupiter.api.Test;
import xyz.erupt.ai_canvas.model.AiCanvasModel;
import xyz.erupt.ai_canvas.service.AiCanvasService;
import xyz.erupt.annotation.fun.PowerObject;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure helpers behind the write support: the operation names the LLM may hand
 * to the write verification tool, the mapping onto the model power flags, and
 * the allowed-writes line composed from a binding's switches.
 *
 * @author YuePeng
 * date 2026/9/6
 */
class CanvasWriteOpTest {

    @Test
    void parsesSdkNamesAndCommonSynonyms() {
        assertEquals(EruptCanvasModelProvider.WriteOp.ADD, EruptCanvasModelProvider.WriteOp.parse("add"));
        assertEquals(EruptCanvasModelProvider.WriteOp.ADD, EruptCanvasModelProvider.WriteOp.parse(" Create "));
        assertEquals(EruptCanvasModelProvider.WriteOp.UPDATE, EruptCanvasModelProvider.WriteOp.parse("update"));
        assertEquals(EruptCanvasModelProvider.WriteOp.UPDATE, EruptCanvasModelProvider.WriteOp.parse("EDIT"));
        assertEquals(EruptCanvasModelProvider.WriteOp.DELETE, EruptCanvasModelProvider.WriteOp.parse("delete"));
        assertEquals(EruptCanvasModelProvider.WriteOp.DELETE, EruptCanvasModelProvider.WriteOp.parse("remove"));
    }

    @Test
    void rejectsUnknownAndBlankOperations() {
        assertNull(EruptCanvasModelProvider.WriteOp.parse("query"));
        assertNull(EruptCanvasModelProvider.WriteOp.parse(""));
        assertNull(EruptCanvasModelProvider.WriteOp.parse(null));
    }

    @Test
    void permissionFollowsTheMatchingPowerFlag() {
        PowerObject power = new PowerObject();
        power.setAdd(false);
        power.setEdit(true);
        power.setDelete(false);
        assertFalse(EruptCanvasModelProvider.WriteOp.ADD.permitted(power));
        assertTrue(EruptCanvasModelProvider.WriteOp.UPDATE.permitted(power));
        assertFalse(EruptCanvasModelProvider.WriteOp.DELETE.permitted(power));
    }

    @Test
    void allowedWritesListsSwitchedOnOperationsInSdkOrder() {
        AiCanvasModel binding = new AiCanvasModel();
        assertTrue(AiCanvasService.allowedWrites(binding).isEmpty());

        binding.setAllowDelete(true);
        binding.setAllowAdd(true);
        assertEquals(List.of("add", "delete"), AiCanvasService.allowedWrites(binding));

        binding.setAllowEdit(true);
        assertEquals(List.of("add", "update", "delete"), AiCanvasService.allowedWrites(binding));
    }

    @Test
    void legacyBindingsWithNullSwitchesAreReadOnly() {
        AiCanvasModel binding = new AiCanvasModel();
        binding.setAllowAdd(null);
        binding.setAllowEdit(null);
        binding.setAllowDelete(null);
        assertTrue(AiCanvasService.allowedWrites(binding).isEmpty());
    }

}
