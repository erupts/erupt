package xyz.erupt.ai_canvas.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The validator decides whether a generated page is filed or sent back for a
 * repair round, so both directions matter: real breakage must be caught and
 * ordinary modern JavaScript must pass untouched.
 *
 * @author YuePeng
 * date 2026/9/6
 */
class CanvasHtmlValidatorTest {

    private static final String ASSETS = """
            <script src="${base}/erupt-canvas-sdk.js"></script>
            <link rel="stylesheet" href="${base}/element-plus/element.min.css">
            <script src="${base}/element-plus/vue3.js"></script>
            <script src="${base}/element-plus/element.min.js"></script>
            """;

    private static String page(String head, String body) {
        return "<!DOCTYPE html><html lang=\"en\"><head>" + head + "</head><body>" + body + "</body></html>";
    }

    @Test
    void completeVuePagePasses() {
        String body = """
                <div id="app"><el-table :data="rows"><el-table-column prop="name"/></el-table></div>
                <script>
                const {createApp, ref} = Vue;
                createApp({
                  setup() {
                    const rows = ref([]);
                    const load = async () => {
                      try {
                        const page = await Erupt.table('Product', {pageIndex: 1, condition: [{key: 'name', value: 'x', expression: 'LIKE'}]});
                        rows.value = page.list ?? [];
                      } catch (e) { ElMessage.error(e?.message || 'failed'); }
                    };
                    const label = (r) => `${r.name} (${r.dept_name ?? '-'}) {open}`;
                    const isCode = /^\\{[a-z]+\\}$/.test('x') || "}".length === 1 || '{'.length;
                    load();
                    return {rows, label, isCode};
                  }
                }).use(ElementPlus).mount('#app');
                </script>
                """;
        assertEquals(List.of(), CanvasHtmlValidator.validate(page(ASSETS, body)));
    }

    @Test
    void truncatedDocumentIsReported() {
        String html = "<!DOCTYPE html><html><head>" + ASSETS + "</head><body><div id=\"app\"></div><script>createApp({";
        List<String> problems = CanvasHtmlValidator.validate(html);
        assertTrue(problems.stream().anyMatch(p -> p.contains("</html>")));
        assertTrue(problems.stream().anyMatch(p -> p.contains("<script>")));
    }

    @Test
    void leakedMarkdownFenceIsReported() {
        List<String> problems = CanvasHtmlValidator.validate(page(ASSETS, "<p>x</p>```"));
        assertEquals(1, problems.size());
        assertTrue(problems.get(0).contains("fence"));
    }

    @Test
    void unbalancedInlineScriptIsReported() {
        String body = "<div id=\"app\"></div><script>Vue.createApp({setup() { return {a: 1}; }).mount('#app');</script>";
        List<String> problems = CanvasHtmlValidator.validate(page(ASSETS, body));
        assertEquals(1, problems.size());
        assertTrue(problems.get(0).startsWith("inline <script> #4"), problems.get(0));
    }

    @Test
    void nonJavaScriptScriptBlocksAreSkipped() {
        String body = """
                <script type="text/x-template" id="tpl"><div>{{ a }</div></script>
                <script type="application/json" id="cfg">{"a": [1, 2</script>
                <div id="app"></div>
                <script>Vue.createApp({template: '#tpl'}).mount('#app');</script>
                """;
        assertEquals(List.of(), CanvasHtmlValidator.validate(page(ASSETS, body)));
    }

    @Test
    void frameworkUsedWithoutItsScriptIsReported() {
        String head = "<script src=\"${base}/erupt-canvas-sdk.js\"></script>";
        String body = "<div id=\"app\"><el-button>x</el-button></div><script>Vue.createApp({}).use(ElementPlus).mount('#app');</script>";
        List<String> problems = CanvasHtmlValidator.validate(page(head, body));
        assertEquals(2, problems.size());
        assertTrue(problems.get(0).contains("Vue"));
        assertTrue(problems.get(1).contains("Element Plus"));
    }

    @Test
    void elementPlusBeforeVueIsReported() {
        String head = """
                <script src="${base}/erupt-canvas-sdk.js"></script>
                <script src="${base}/element-plus/element.min.js"></script>
                <script src="${base}/element-plus/vue3.js"></script>
                """;
        List<String> problems = CanvasHtmlValidator.validate(page(head, "<div id=\"app\"></div><script>Vue.createApp({}).mount('#app')</script>"));
        assertEquals(1, problems.size());
        assertTrue(problems.get(0).contains("before vue3.js"));
    }

    @Test
    void bracketScannerUnderstandsLiterals() {
        assertNull(CanvasHtmlValidator.bracketProblem("const s = 'a { b'; const t = \"c ) d\"; const r = /[{(]/g; x = a / b / c;"));
        assertNull(CanvasHtmlValidator.bracketProblem("const t = `outer ${fn({a: `inner ${b}`})} }`; // comment {\n/* block ( */"));
        assertNull(CanvasHtmlValidator.bracketProblem("return /\\}/.test(s) ? 1 : 2; if (x) return (a) / 2;"));
        assertNotNull(CanvasHtmlValidator.bracketProblem("function f() { if (a) { return 1; }"));
        assertNotNull(CanvasHtmlValidator.bracketProblem("const x = [1, 2, 3);"));
        assertNotNull(CanvasHtmlValidator.bracketProblem("const s = `unterminated ${a}"));
        assertNotNull(CanvasHtmlValidator.bracketProblem("const s = 'cut\noff';"));
    }

    @Test
    void problemsOfCoversAnswerLevelFailures() {
        assertEquals(List.of("the answer was empty"), AiCanvasService.problemsOf("  ", false));
        assertTrue(AiCanvasService.problemsOf("Sure, here is the page:", false).get(0).contains("no complete HTML document"));
        List<String> cutOff = AiCanvasService.problemsOf("```html\n" + page(ASSETS, "<p>ok</p>") + "\n```", true);
        assertEquals(1, cutOff.size());
        assertTrue(cutOff.get(0).contains("cut off"));
        assertEquals(List.of(), AiCanvasService.problemsOf("```html\n" + page(ASSETS, "<p>ok</p>") + "\n```", false));
        String repair = AiCanvasService.repairMessage(List.of("problem one", "problem two"));
        assertTrue(repair.contains("- problem one\n- problem two\n"));
    }

}
