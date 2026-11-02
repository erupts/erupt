package xyz.erupt.share.handler;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.share.model.EruptShareLink;

import java.util.List;

/**
 * The "Open Link" row button: opens the link in a new tab, the way a visitor will see it.
 *
 * @author YuePeng
 * date 2026-11-01
 */
@Component
public class ShareOpenLinkOperation implements OperationHandler<EruptShareLink, Void> {

    public static final String CODE = "openLink";

    @Resource
    private EruptDao eruptDao;

    @Override
    public String exec(List<EruptShareLink> data, Void unused, String[] param) {
        EruptShareLink link = eruptDao.find(EruptShareLink.class, data.get(0).getId());
        return "window.open(" + ShareLinkRoute.urlExpression(link) + ")";
    }

}
