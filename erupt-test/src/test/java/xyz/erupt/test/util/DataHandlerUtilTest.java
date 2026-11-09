package xyz.erupt.test.util;

import org.junit.jupiter.api.Test;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.util.DataHandlerUtil;
import xyz.erupt.test.EruptApplicationTests;
import xyz.erupt.test.util.model.PasswordViews;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link DataHandlerUtil#convertDataToEruptView}: the row keys of PASSWORD views are masked, and nothing else.
 *
 * @author YuePeng
 */
class DataHandlerUtilTest extends EruptApplicationTests {

    @Test
    void onlyThePasswordViewsAreMasked() {
        Map<String, Object> row = new HashMap<>(Map.of("id", 1, "secret", "s3cret", "secret_note", "visible",
                "owner_name", "Ann", "owner_account", "ann"));
        DataHandlerUtil.convertDataToEruptView(EruptCoreService.getErupt(PasswordViews.class.getSimpleName()), List.of(row));
        assertEquals(EruptConst.PASSWORD_PLACEHOLDER, row.get("secret"));
        assertEquals(EruptConst.PASSWORD_PLACEHOLDER, row.get("owner_account"));
        // a snake_case field of its own, and the other column of the same reference, keep their values
        assertEquals("visible", row.get("secret_note"));
        assertEquals("Ann", row.get("owner_name"));
        assertEquals(1, row.get("id"));
    }

}
