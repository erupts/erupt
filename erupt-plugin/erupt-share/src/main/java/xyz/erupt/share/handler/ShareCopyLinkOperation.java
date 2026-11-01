package xyz.erupt.share.handler;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.share.model.EruptShareLink;

import java.util.List;

/**
 * The "Copy Link" row button: puts the link's full URL on the clipboard through the page's copy
 * helper, which falls back to a selection copy on plain http and tells the user when it cannot.
 *
 * @author YuePeng
 */
@Component
public class ShareCopyLinkOperation implements OperationHandler<EruptShareLink, Void> {

    public static final String CODE = "copyLink";

    @Resource
    private EruptDao eruptDao;

    @Override
    public String exec(List<EruptShareLink> data, Void unused, String[] param) {
        EruptShareLink link = eruptDao.find(EruptShareLink.class, data.get(0).getId());
        return "copy(" + ShareLinkRoute.urlExpression(link) + ", '" + ShareLinkRoute.js(I18nTranslate.$translate("Link copied")) + "')";
    }

}
