package xyz.erupt.security.scope;

import jakarta.servlet.http.HttpServletRequest;
import xyz.erupt.core.annotation.EruptRouter;

/**
 * Narrows what a verified session may call. A plain login session has no scope and every
 * guard lets it through; a session minted for a narrower purpose (a public share link, for
 * example) carries one, and the module that minted it refuses everything outside it here.
 * <p>
 * Runs in the security interceptor right after the token has been verified and the user
 * registered, before the menu and erupt permission checks: a refusal answers 403.
 *
 * @author YuePeng
 */
public interface SessionScopeGuard {

    /**
     * @param token       the verified session token
     * @param request     the current request
     * @param eruptRouter the route's verification rule
     * @return false to refuse the request
     */
    boolean allow(String token, HttpServletRequest request, EruptRouter eruptRouter);

}
