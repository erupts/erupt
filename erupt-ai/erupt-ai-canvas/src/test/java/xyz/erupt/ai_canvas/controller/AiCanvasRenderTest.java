package xyz.erupt.ai_canvas.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Render-time normalization of stored pages: the SDK tag must land inside a
 * real head so nothing precedes the doctype, whatever the model produced.
 *
 * @author YuePeng
 * date 2026/9/6
 */
class AiCanvasRenderTest {

    @Test
    void headWithAttributesIsRecognized() {
        String html = "<!DOCTYPE html><html><HEAD lang=\"en\"><title>t</title></HEAD><body></body></html>";
        String out = AiCanvasController.render(html, "/ctx");
        assertTrue(out.startsWith("<!DOCTYPE html><html><HEAD lang=\"en\"><script src=\"/ctx" + AiCanvasController.SDK_PATH));
    }

    @Test
    void missingHeadAndDoctypeAreSynthesized() {
        String out = AiCanvasController.render("\n<html lang=\"en\"><body><p>x</p></body></html>", "");
        assertTrue(out.startsWith("<!DOCTYPE html>\n<html lang=\"en\"><head><script src=\"" + AiCanvasController.SDK_PATH));
        assertTrue(out.contains("</head><body>"));
    }

    @Test
    void bodyOnlyFragmentGetsAHeadBeforeTheBody() {
        String out = AiCanvasController.normalizeDocument("<body><p>x</p></body>");
        assertEquals("<!DOCTYPE html>\n<head></head><body><p>x</p></body>", out);
    }

    @Test
    void existingSdkTagIsNotDuplicated() {
        String html = "<!DOCTYPE html><html><head><script src=\"${base}" + AiCanvasController.SDK_PATH + "\"></script></head><body></body></html>";
        String out = AiCanvasController.render(html, "/app");
        assertEquals(1, out.split("erupt-canvas-sdk\\.js", -1).length - 1);
        assertTrue(out.contains("src=\"/app" + AiCanvasController.SDK_PATH + "?v="));
    }

}
