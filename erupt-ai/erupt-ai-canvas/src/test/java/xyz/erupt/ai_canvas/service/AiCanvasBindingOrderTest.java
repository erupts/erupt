package xyz.erupt.ai_canvas.service;

import org.junit.jupiter.api.Test;
import xyz.erupt.ai_canvas.model.AiCanvas;
import xyz.erupt.ai_canvas.model.AiCanvasModel;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prompt composition relies on a stable binding order and on grouping by data
 * source type; both are pure helpers exercised here.
 *
 * @author YuePeng
 * date 2026/9/6
 */
class AiCanvasBindingOrderTest {

    private static AiCanvasModel binding(Long id, String type, String model) {
        AiCanvasModel m = new AiCanvasModel();
        m.setId(id);
        m.setDataType(type);
        m.setModel(model);
        return m;
    }

    @Test
    void noBindingsYieldsEmptyList() {
        assertTrue(AiCanvasService.orderedBindings(new AiCanvas()).isEmpty());
    }

    @Test
    void orderedByIdWithUnsavedLast() {
        AiCanvas canvas = new AiCanvas();
        canvas.setModels(new LinkedHashSet<>(List.of(
                binding(null, "erupt", "New"),
                binding(3L, "erupt", "Third"),
                binding(1L, "erupt", "First"))));
        List<String> models = AiCanvasService.orderedBindings(canvas).stream().map(AiCanvasModel::getModel).toList();
        assertEquals(List.of("First", "Third", "New"), models);
    }

    @Test
    void groupsByTypeInFirstSeenOrder() {
        Map<String, List<AiCanvasModel>> byType = AiCanvasService.groupByType(List.of(
                binding(1L, "sql", "Orders"),
                binding(2L, "erupt", "Product"),
                binding(3L, "sql", "Customers")));
        assertEquals(List.of("sql", "erupt"), byType.keySet().stream().toList());
        assertEquals(2, byType.get("sql").size());
        assertEquals("Product", byType.get("erupt").get(0).getModel());
    }

}
