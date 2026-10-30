package xyz.erupt.share.handler;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.share.model.EruptShareLink;

import java.util.List;

/**
 * The "Copy Link" row button: puts the link's full URL on the clipboard. The URL is assembled in
 * the browser, since only the page knows the address the admin app is served from; the server
 * contributes the route and the wording.
 *
 * @author YuePeng
 */
@Component
public class ShareCopyLinkOperation implements OperationHandler<EruptShareLink, Void> {

    public static final String CODE = "copyLink";

    // Hash route of the landing page, appended to the admin app's own address
    private static final String ROUTE = "#/share/";

    @Resource
    private EruptDao eruptDao;

    @Override
    public String exec(List<EruptShareLink> data, Void unused, String[] param) {
        EruptShareLink link = eruptDao.find(EruptShareLink.class, data.get(0).getId());
        String url = "location.origin + location.pathname + '" + ROUTE + link.getCode() + "'";
        // the clipboard is withheld on an insecure origin: the URL is then shown to copy by hand
        return "const url = " + url + "; navigator.clipboard.writeText(url).then(() => msg.success('"
                + js(I18nTranslate.$translate("Link copied")) + "'), () => codeModal('text', url))";
    }

    private static String js(String text) {
        return text.replace("\\", "\\\\").replace("'", "\\'");
    }
}
