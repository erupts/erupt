package xyz.erupt.ai_canvas.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Render-time injection into stored pages: the SDK tag lands right after
 * {@code <head>}, is versioned, and is never duplicated.
 *
 * @author YuePeng
 * date 2026/9/6
 */
class AiCanvasRenderTest {

    @Test
    void sdkIsInjectedAtHeadStart() {
        String html = "<!DOCTYPE html><html><head><title>t</title></head><body></body></html>";
        String out = AiCanvasController.render(html, "/ctx");
        assertTrue(out.startsWith("<!DOCTYPE html><html><head><script src=\"/ctx" + AiCanvasController.SDK_PATH + "?v="));
    }

    @Test
    void basePlaceholderIsResolved() {
        String out = AiCanvasController.render("<html><head></head><body><img src=\"${base}/x.png\"></body></html>", "/app");
        assertTrue(out.contains("src=\"/app/x.png\""));
        assertFalse(out.contains("${base}"));
    }

    @Test
    void existingSdkTagIsNotDuplicated() {
        String html = "<!DOCTYPE html><html><head><script src=\"${base}" + AiCanvasController.SDK_PATH + "\"></script></head><body></body></html>";
        String out = AiCanvasController.render(html, "/app");
        assertEquals(1, out.split("erupt-canvas-sdk\\.js", -1).length - 1);
        assertTrue(out.contains("src=\"/app" + AiCanvasController.SDK_PATH + "?v="));
    }

}
