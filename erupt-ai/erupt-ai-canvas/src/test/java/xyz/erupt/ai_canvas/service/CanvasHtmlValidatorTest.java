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
                <div id="app"></div>
                <script type="text/x-template" id="page-tpl">
                  <el-table :data="rows"><el-table-column prop="name" :someProp="1" /></el-table>
                </script>
                <script>
                const {createApp, ref} = Vue;
                const {ElMessage} = ElementPlus;
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
                  },
                  template: '#page-tpl'
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
    void missingHeadIsReported() {
        String html = "<!DOCTYPE html><html><body>" + ASSETS + "<div id=\"app\"></div></body></html>";
        List<String> problems = CanvasHtmlValidator.validate(html);
        assertTrue(problems.stream().anyMatch(p -> p.contains("<head>")));
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
        String body = "<div id=\"app\"></div><script type=\"text/x-template\" id=\"t\"><el-button>x</el-button></script>"
                + "<script>Vue.createApp({template: '#t'}).use(ElementPlus).mount('#app');</script>";
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
    void vue2DialogBindingIsReported() {
        // The exact shape that renders fine and never opens: Vue 3 drops `.sync` silently
        String body = """
                <div id="app"></div>
                <script type="text/x-template" id="t">
                  <div>
                    <el-button @click="show = true">new</el-button>
                    <el-dialog :visible.sync="show" title="t"><p>x</p></el-dialog>
                  </div>
                </script>
                <script>Vue.createApp({template: '#t', data() { return {show: false}; }}).use(ElementPlus).mount('#app');</script>
                """;
        List<String> problems = CanvasHtmlValidator.validate(page(ASSETS, body));
        assertEquals(1, problems.size());
        assertTrue(problems.get(0).contains(".sync"), problems.get(0));
        assertTrue(problems.get(0).contains("v-model"), problems.get(0));
    }

    @Test
    void otherVue2LeftoversAreReported() {
        assertTrue(firstProblem("<span @click.native=\"go\"></span>").contains(".native"));
        assertTrue(firstProblem("<el-table><template slot=\"header\"><b/></template></el-table>").contains("slot="));
        assertTrue(firstProblem("<el-table><template slot-scope=\"s\"><b/></template></el-table>").contains("slot-scope"));
        assertTrue(firstProblem("<script>new Vue({el: '#x'});</script>").contains("new Vue"));
        assertTrue(firstProblem("<script>Vue.component('x', {});</script>").contains("global API"));
        assertTrue(firstProblem("<el-button icon=\"el-icon-plus\">add</el-button>").contains("icon font"));
        assertTrue(firstProblem("<i class=\"el-icon-search\"></i>").contains("icon font"));
    }

    @Test
    void bareElementPlusHelpersAreReported() {
        String body = "<div id=\"app\"></div><script>Vue.createApp({methods: {f() { ElMessage.success('ok'); }}}).mount('#app');</script>";
        List<String> problems = CanvasHtmlValidator.validate(page(ASSETS, body));
        assertEquals(1, problems.size());
        assertTrue(problems.get(0).contains("ElMessage"), problems.get(0));
        assertTrue(problems.get(0).contains("ElementPlus"), problems.get(0));
    }

    @Test
    void destructuredOrQualifiedHelpersPass() {
        String destructured = "<div id=\"app\"></div><script>const {ElMessage, ElMessageBox} = ElementPlus;"
                + " Vue.createApp({methods: {f() { ElMessage.success('ok'); ElMessageBox.confirm('y'); }}}).use(ElementPlus).mount('#app');</script>";
        assertEquals(List.of(), CanvasHtmlValidator.validate(page(ASSETS, destructured)));
        String qualified = "<div id=\"app\"></div><script>Vue.createApp({methods: {f() { ElementPlus.ElMessage.success('ok'); }}}).use(ElementPlus).mount('#app');</script>";
        assertEquals(List.of(), CanvasHtmlValidator.validate(page(ASSETS, qualified)));
    }

    private static final String ICON_ASSETS = ASSETS + "<script src=\"${base}/element-plus/element-icons.min.js\"></script>\n";

    private static String iconPage(String head, String setup) {
        return page(head, "<div id=\"app\"></div>"
                + "<script type=\"text/x-template\" id=\"t\"><el-button><el-icon><Plus /></el-icon>add</el-button></script>"
                + "<script>const app = Vue.createApp({template: '#t'}); app.use(ElementPlus); " + setup + " app.mount('#app');</script>");
    }

    @Test
    void iconComponentsWithoutTheirScriptAreReported() {
        List<String> problems = CanvasHtmlValidator.validate(iconPage(ASSETS, ""));
        assertEquals(1, problems.size());
        assertTrue(problems.get(0).contains("element-icons.min.js"), problems.get(0));
        assertTrue(problems.get(0).contains("Plus"), problems.get(0));
    }

    @Test
    void unregisteredIconComponentsAreReported() {
        List<String> problems = CanvasHtmlValidator.validate(iconPage(ICON_ASSETS, ""));
        assertEquals(1, problems.size());
        assertTrue(problems.get(0).contains("never registers them"), problems.get(0));
    }

    @Test
    void registeredIconComponentsPass() {
        String register = "Object.entries(ElementPlusIconsVue).forEach(([n, c]) => app.component(n, c));";
        assertEquals(List.of(), CanvasHtmlValidator.validate(iconPage(ICON_ASSETS, register)));
    }

    @Test
    void iconBundleIsNotMistakenForTheCoreBundle() {
        // element-icons.min.js also contains "element"; loading only it must still
        // report the missing core bundle rather than silently passing
        String head = "<script src=\"${base}/erupt-canvas-sdk.js\"></script>"
                + "<script src=\"${base}/element-plus/vue3.js\"></script>"
                + "<script src=\"${base}/element-plus/element-icons.min.js\"></script>";
        String body = "<div id=\"app\"></div><script type=\"text/x-template\" id=\"t\"><el-button>x</el-button></script>"
                + "<script>Vue.createApp({template: '#t'}).use(ElementPlus).mount('#app');</script>";
        List<String> problems = CanvasHtmlValidator.validate(page(head, body));
        assertEquals(1, problems.size());
        assertTrue(problems.get(0).contains("loads no Element Plus script"), problems.get(0));
    }

    @Test
    void inDomTemplateIsReported() {
        // Each of these markers means the browser, not Vue, parses the markup first
        for (String markup : List.of(
                "<el-table :data=\"rows\"></el-table>",
                "<p>{{ total }}</p>",
                "<span v-if=\"ok\">y</span>",
                "<button @click=\"go\">go</button>")) {
            String body = "<div id=\"app\">" + markup + "</div>"
                    + "<script>Vue.createApp({}).use(ElementPlus).mount('#app');</script>";
            List<String> problems = CanvasHtmlValidator.validate(page(ASSETS, body));
            assertEquals(1, problems.size(), markup);
            assertTrue(problems.get(0).contains("text/x-template"), problems.get(0));
        }
    }

    @Test
    void emptyMountElementAndPlainHtmlPass() {
        // An x-template page and a page that uses no Vue at all must both stay clean
        String xTemplate = "<div id=\"app\"></div><script type=\"text/x-template\" id=\"t\">"
                + "<el-table :data=\"rows\"><el-table-column prop=\"n\" :someProp=\"1\" /></el-table></script>"
                + "<script>Vue.createApp({template: '#t', data: () => ({rows: []})}).use(ElementPlus).mount('#app');</script>";
        assertEquals(List.of(), CanvasHtmlValidator.validate(page(ASSETS, xTemplate)));

        String plain = "<table id=\"grid\"><tr><td>x</td></tr></table>"
                + "<script>Erupt.table('P', {}).then(p => { document.getElementById('grid').textContent = p.total; });</script>";
        assertEquals(List.of(), CanvasHtmlValidator.validate(page(ASSETS, plain)));
    }

    /** Wraps a markup snippet the way a correct page would, so only the tested defect fires */
    private static String firstProblem(String snippet) {
        String body = "<div id=\"app\"></div><script type=\"text/x-template\" id=\"t\"><div>" + snippet
                + "</div></script><script>Vue.createApp({template: '#t'}).use(ElementPlus).mount('#app');</script>";
        List<String> problems = CanvasHtmlValidator.validate(page(ASSETS, body));
        assertFalse(problems.isEmpty(), "expected a problem for: " + snippet);
        return problems.get(0);
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
