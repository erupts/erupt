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
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.service.EruptModifyService;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.test.EruptApplicationTests;
import xyz.erupt.test.model.edit.*;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The ranges, lengths and tag lists an edit form declares hold for API writes too, and sub-table
 * rows are checked like records of their own.
 */
@Transactional
class ValueValidateTest extends EruptApplicationTests {

    @Resource
    private EruptModifyService eruptModifyService;

    @BeforeEach
    void user() {
        MetaContext.register(new MetaUser(1L, "admin", "admin"));
    }

    @AfterEach
    void clear() {
        MetaContext.remove();
    }

    private static EruptModel model(Class<?> clazz) {
        return EruptCoreService.getErupt(clazz.getSimpleName());
    }

    private static JsonObject one(String field, Object value) {
        JsonObject data = new JsonObject();
        if (value instanceof Number n) data.addProperty(field, n);
        else data.addProperty(field, String.valueOf(value));
        return data;
    }

    private void add(Class<?> clazz, JsonObject data) {
        eruptModifyService.insertEruptData(model(clazz), data);
    }

    private void refused(Class<?> clazz, JsonObject data) {
        assertThrows(EruptApiErrorTip.class, () -> this.add(clazz, data), data.toString());
    }

    @Test
    void numberRangeOnlyWhereDeclared() {
        JsonObject ok = one("age", 150);
        ok.addProperty("quantity", 1);
        this.add(NumberModel.class, ok);
        JsonObject over = one("age", 151);
        over.addProperty("quantity", 1);
        this.refused(NumberModel.class, over);
        JsonObject negative = one("quantity", -1);
        this.refused(NumberModel.class, negative);
    }

    @Test
    void sliderAndRate() {
        this.add(SliderModel.class, one("percentage", 100));
        this.refused(SliderModel.class, one("percentage", 101));

        this.add(RateModel.class, one("score", 10));
        this.refused(RateModel.class, one("rating", 6));
        this.refused(RateModel.class, one("rating", 2.5));
        this.add(RateModel.class, one("halfStar", 3));
    }

    @Test
    void inputLength() {
        JsonObject ok = one("name", "n");
        ok.addProperty("description", "d".repeat(500));
        this.add(InputModel.class, ok);
        JsonObject over = one("name", "n");
        over.addProperty("description", "d".repeat(501));
        this.refused(InputModel.class, over);
    }

    @Test
    void tagsWithoutExtensionStayInTheList() {
        this.add(TagsModel.class, one("category", "[\"Tech\"]"));
        this.refused(TagsModel.class, one("category", "[\"Tech\",\"Space\"]"));
        this.add(TagsModel.class, one("skills", "[\"Java\",\"Anything\"]"));
    }

    @Test
    void futureAndHistoryDates() {
        this.add(DateModel.class, one("expireDate", LocalDate.now().plusDays(1).toString()));
        this.refused(DateModel.class, one("expireDate", LocalDate.now().minusDays(1).toString()));
        this.refused(DateModel.class, one("birthDate", LocalDate.now().plusDays(1).toString()));
    }

    @Test
    void subTableRowsAreChecked() {
        JsonObject data = new JsonObject();
        JsonArray children = new JsonArray();
        JsonObject row = new JsonObject();
        row.addProperty("description", "no name");
        children.add(row);
        data.add("children", children);
        EruptApiErrorTip e = assertThrows(EruptApiErrorTip.class, () -> this.add(TabTableAddModel.class, data));
        assertTrue(e.getMessage().contains("#1"), "the failing row is named: " + e.getMessage());

        row.addProperty("name", "named");
        this.add(TabTableAddModel.class, data);
    }

    @Test
    void anUnchangedOutOfRangeValueDoesNotBlockAnEdit() {
        NumberModel old = new NumberModel();
        old.setAge(200);
        old.setQuantity(1);
        eruptDao.persist(old);
        eruptDao.flush();

        JsonObject edit = one("age", 200);
        edit.addProperty("id", old.getId());
        edit.addProperty("quantity", 2);
        eruptModifyService.updateEruptData(model(NumberModel.class), edit);
        assertEquals(2, eruptDao.find(NumberModel.class, old.getId()).getQuantity());
    }

}
