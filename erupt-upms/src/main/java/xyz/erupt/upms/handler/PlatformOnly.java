package xyz.erupt.upms.handler;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.expr.ExprBool;
import xyz.erupt.upms.helper.UpmsTenant;
import xyz.erupt.upms.tenant.UpmsTenantIdentifierResolver;

/**
 * Renders a field for the platform alone: a tenant neither sees it nor decides it. On the site
 * settings it also marks the fields a tenant's own settings can never override (maintenance mode).
 *
 * @author YuePeng
 * date 2026/11/23
 */
@Service
@Comment("Renders only in a platform session, never in a tenant's")
public class PlatformOnly implements ExprBool.ExprHandler {

    @Resource
    private UpmsTenantIdentifierResolver tenantResolver;

    @Override
    public boolean handler(boolean expr, String[] params) {
        return UpmsTenant.isPlatform(tenantResolver.resolveCurrentTenantIdentifier());
    }

}
