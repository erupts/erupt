package xyz.erupt.ai_canvas.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Locks the HTML extraction rules applied to LLM responses:
 * fenced ```html blocks win, bare documents are the fallback,
 * anything else yields null (the service then raises bad_response).
 */
public class AiCanvasHtmlExtractTest {

    private static final String DOC = "<!DOCTYPE html>\n<html><body>hi</body></html>";

    @Test
    public void fencedBlockIsExtractedAndTrimmed() {
        String response = "Here is the page:\n```html\n" + DOC + "\n```\nDone.";
        assertEquals(DOC, AiCanvasService.findHtmlDocument(response));
    }

    @Test
    public void lastFenceClosesTheBlockEvenWithInnerBackticks() {
        String inner = "<html><body><pre>```js\ncode\n```</pre></body></html>";
        String response = "```html\n" + inner + "\n```";
        assertEquals(inner, AiCanvasService.findHtmlDocument(response));
    }

    @Test
    public void bareDoctypeDocumentIsExtracted() {
        String response = "Sure, here you go:\n" + DOC + "\nAnything else?";
        assertEquals(DOC, AiCanvasService.findHtmlDocument(response));
    }

    @Test
    public void bareHtmlTagWithoutDoctypeIsExtracted() {
        String doc = "<html><head></head><body>x</body></html>";
        assertEquals(doc, AiCanvasService.findHtmlDocument("prefix " + doc + " suffix"));
    }

    @Test
    public void unclosedFenceFallsBackToBareDocument() {
        // Truncated output: fence opened but never closed, yet the document is complete
        String response = "```html\n" + DOC;
        assertEquals(DOC, AiCanvasService.findHtmlDocument(response));
    }

    @Test
    public void noDocumentYieldsNull() {
        assertNull(AiCanvasService.findHtmlDocument("I cannot generate that page."));
        assertNull(AiCanvasService.findHtmlDocument("<html><body>never closed"));
        assertNull(AiCanvasService.findHtmlDocument("</html> before <html>"));
    }

}
