package xyz.erupt.share.handler;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.FilterHandler;
import xyz.erupt.core.module.MetaUserinfo;
import xyz.erupt.share.model.EruptShareLink;
import xyz.erupt.upms.helper.UpmsTenant;
import xyz.erupt.upms.service.EruptUserService;

/**
 * Row scope of the share link admin page: a tenant sees its own links only, and inside it an
 * administrator sees every link while other users see the ones they issued.
 *
 * @author YuePeng
 */
@Component
public class ShareScopeFilter implements FilterHandler {

    @Resource
    private EruptUserService eruptUserService;

    @Override
    public String filter(String condition, String[] params) {
        MetaUserinfo user = eruptUserService.getSimpleUserInfo();
        String alias = EruptShareLink.class.getSimpleName();
        // the tenant id comes from the session store, never from the request; quoted all the same
        String tenant = UpmsTenant.normalize(user.getTenantId()).replace("'", "''");
        String scope = alias + ".tenantId = '" + tenant + "'";
        if (!user.isSuperAdmin()) {
            scope += " and " + alias + ".createUser.id = " + user.getId();
        }
        return scope;
    }
}
