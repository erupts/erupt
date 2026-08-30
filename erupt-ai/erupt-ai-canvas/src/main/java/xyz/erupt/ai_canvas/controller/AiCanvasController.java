package xyz.erupt.ai_canvas.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.ai_canvas.model.AiCanvas;
import xyz.erupt.ai_canvas.service.AiCanvasService;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.jpa.dao.EruptDao;

/**
 * Returns the published page HTML stored in the database. The frontend route
 * {@code #/ai/canvas/{code}} fetches it and embeds it in an iframe; the page
 * itself calls the Erupt data APIs with the visitor's own token, so data
 * permissions still follow the logged-in user. Draft versions being iterated
 * in the designer are never served here — only the explicitly published copy.
 *
 * @author YuePeng
 * date 2026/8/3
 */
@RestController
public class AiCanvasController {

    public static final String HTML_PATH = EruptRestPath.ERUPT_API + "/ai-canvas/html";

    // Served from resources/public as a plain static asset
    public static final String SDK_PATH = "/erupt-canvas-sdk.js";

    // Placeholder kept verbatim in stored HTML, swapped for the context path at render time
    private static final String BASE_PLACEHOLDER = "${base}";

    // Static resources are served with a month-long cache; version the SDK URL per
    // backend boot so browsers pick up SDK changes instead of a stale cached copy
    private static final String SDK_VERSION_PARAM = "?v=" + System.currentTimeMillis();

    @Resource
    private EruptDao eruptDao;

    @Resource
    private AiCanvasService aiCanvasService;

    @GetMapping(value = HTML_PATH + "/{code}", produces = "text/html;charset=utf-8")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public String html(@PathVariable("code") String code, HttpServletRequest request) {
        AiCanvas view = eruptDao.lambdaQuery(AiCanvas.class).eq(AiCanvas::getCode, code).one();
        if (null == view) {
            return tip(I18nTranslate.$translate("ai-canvas.not_generated"));
        }
        if (!Boolean.TRUE.equals(view.getEnable())) {
            return tip(I18nTranslate.$translate("ai-canvas.disabled"));
        }
        String publishedHtml = aiCanvasService.publishedHtml(view);
        if (StringUtils.isBlank(publishedHtml)) {
            return tip(I18nTranslate.$translate("ai-canvas.not_published"));
        }
        return render(publishedHtml, request.getContextPath());
    }

    // Shared with the designer's draft preview endpoint in AiCanvasBuildController
    public static String render(String html, String contextPath) {
        html = html.replace(BASE_PLACEHOLDER, contextPath);
        html = ensureSdk(html, contextPath);
        html = html.replace(SDK_PATH, SDK_PATH + SDK_VERSION_PARAM);
        // injectToken runs last so its tag lands before the SDK tag at the head start
        return injectToken(html);
    }

    // The page is often embedded via iframe srcdoc, where URL params don't exist;
    // this endpoint is already token-authenticated, so hand the caller's token
    // to the SDK explicitly instead of letting it fish around for one
    private static String injectToken(String html) {
        String token = MetaContext.getToken();
        if (StringUtils.isBlank(token)) return html;
        return insertAtHead(html,
                "<script>window.eruptToken=\"" + token.replaceAll("[\"'<>\\\\]", "") + "\"</script>");
    }

    // The generated page should reference the SDK itself; inject it when missing
    // so a page that forgot the script tag still works
    private static String ensureSdk(String html, String contextPath) {
        if (html.contains(SDK_PATH)) return html;
        return insertAtHead(html, "<script src=\"" + contextPath + SDK_PATH + "\"></script>");
    }

    private static String insertAtHead(String html, String tag) {
        int head = html.indexOf("<head>");
        if (head >= 0) {
            int insertAt = head + "<head>".length();
            return html.substring(0, insertAt) + tag + html.substring(insertAt);
        }
        return tag + html;
    }

    public static String tip(String message) {
        return "<!DOCTYPE html><html lang=\"en\"><body style=\"display:flex;align-items:center;justify-content:center;height:96vh;margin:0;font-family:sans-serif;color:#999\">"
                + message + "</body></html>";
    }

}
