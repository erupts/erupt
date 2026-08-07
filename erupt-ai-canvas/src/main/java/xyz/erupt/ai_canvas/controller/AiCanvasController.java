package xyz.erupt.ai_canvas.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.ai_canvas.model.AiCanvas;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.jpa.dao.EruptDao;

/**
 * Serves AI generated pages stored in the database. The page itself calls the
 * Erupt data APIs with the visitor's own token, so data permissions still
 * follow the logged-in user.
 *
 * @author YuePeng
 * date 2026/8/3
 */
@RestController
public class AiCanvasController {

    public static final String RENDER_PATH = EruptRestPath.ERUPT_API + "/ai-canvas/render";

    // Served from resources/public as a plain static asset
    public static final String SDK_PATH = "/erupt-canvas-sdk.js";

    // Placeholder kept verbatim in stored HTML, swapped for the context path at render time
    private static final String BASE_PLACEHOLDER = "${base}";

    @Resource
    private EruptDao eruptDao;

    @GetMapping(value = RENDER_PATH + "/{id}", produces = "text/html;charset=utf-8")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN, verifyMethod = EruptRouter.VerifyMethod.PARAM)
    public String render(@PathVariable("id") Long id, HttpServletRequest request) {
        AiCanvas view = eruptDao.find(AiCanvas.class, id);
        if (null == view || StringUtils.isBlank(view.getHtml())) {
            return this.tip(I18nTranslate.$translate("ai-canvas.not_generated"));
        }
        if (!Boolean.TRUE.equals(view.getEnable())) {
            return this.tip(I18nTranslate.$translate("ai-canvas.disabled"));
        }
        String html = view.getHtml().replace(BASE_PLACEHOLDER, request.getContextPath());
        return this.ensureSdk(html, request.getContextPath());
    }

    // The generated page should reference the SDK itself; inject it when missing
    // so a page that forgot the script tag still works
    private String ensureSdk(String html, String contextPath) {
        if (html.contains(SDK_PATH)) return html;
        String sdkTag = "<script src=\"" + contextPath + SDK_PATH + "\"></script>";
        int head = html.indexOf("<head>");
        if (head >= 0) {
            int insertAt = head + "<head>".length();
            return html.substring(0, insertAt) + sdkTag + html.substring(insertAt);
        }
        return sdkTag + html;
    }

    private String tip(String message) {
        return "<!DOCTYPE html><html lang=\"en\"><body style=\"display:flex;align-items:center;justify-content:center;height:96vh;margin:0;font-family:sans-serif;color:#999\">"
                + message + "</body></html>";
    }

}
