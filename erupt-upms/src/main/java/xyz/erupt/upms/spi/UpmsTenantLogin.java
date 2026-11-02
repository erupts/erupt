package xyz.erupt.upms.spi;

import xyz.erupt.upms.base.LoginModel;
import xyz.erupt.upms.model.EruptUser;

/**
 * How a sign-in that does not go through the tenant's password form, single sign-on for one,
 * reaches a tenant. The user directory is shared, so finding the user is the caller's work in a
 * session of the tenant; what only the tenant module knows is which tenant a code names, whether
 * that tenant may sign in at all, and what a tenant session looks like. A deployment without
 * tenants registers no implementation, and every such sign-in is the platform's.
 *
 * @author YuePeng
 */
public interface UpmsTenantLogin {

    /**
     * @param tenantCode the code a user types on the tenant login page
     * @return the tenant's id, or {@code null} when no tenant has that code
     */
    String tenantId(String tenantCode);

    /**
     * Opens the session of a tenant user the caller has already identified: refuses a locked or
     * expired tenant and an unusable account, otherwise issues the tenant token and records the
     * sign-in.
     *
     * @return the outcome, with the reason when it did not pass
     */
    LoginModel login(String tenantId, EruptUser user);

    /**
     * @return the tenant login page for the code, relative to the application's context path
     */
    String loginPage(String tenantCode);

}
