package xyz.erupt.share.handler;

import xyz.erupt.share.model.EruptShareLink;

/**
 * The address of a share link, as a JS expression: it is assembled in the browser, since only the
 * page knows where the admin app is served from; the server contributes the route and the code.
 *
 * @author YuePeng
 * date 2026-11-01
 */
final class ShareLinkRoute {

    // Hash route of the landing page, appended to the admin app's own address
    private static final String ROUTE = "#/share/";

    private ShareLinkRoute() {
    }

    static String urlExpression(EruptShareLink link) {
        return "location.origin + location.pathname + '" + ROUTE + link.getCode() + "'";
    }

    static String js(String text) {
        return text.replace("\\", "\\\\").replace("'", "\\'");
    }

}
