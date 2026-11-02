package xyz.erupt.sso.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.upms.base.LoginModel;
import xyz.erupt.sso.vo.SsoTicketBody;
import xyz.erupt.sso.service.EruptSsoService;
import xyz.erupt.sso.vo.EruptSsoProviderVo;

import java.io.IOException;
import java.util.List;

/**
 * Sign-on endpoints. All of them are anonymous by design: they are what a user without a
 * session has to reach, and each one carries its own proof — a state, a ticket, a signed
 * assertion or nothing worth protecting.
 *
 * <p>The tenant login page passes its tenant code to {@code providers} and {@code authorize}. An
 * OAuth2 provider of a tenant calls back on the platform's address, the tenant being in the state;
 * a SAML provider of a tenant uses the tenant's own path, {@code /sso/tenant/{tenant}/...}. Without a
 * tenant code everything is the platform's.
 *
 * @author YuePeng
 * date 2026-09-18
 */
@RestController
@RequestMapping(EruptRestPath.ERUPT_API + "/sso")
public class EruptSsoController {

    @Resource
    private EruptSsoService eruptSsoService;

    @GetMapping("/providers")
    public List<EruptSsoProviderVo> providers(@RequestParam(value = "tenant", required = false) String tenant) {
        return eruptSsoService.providers(tenant);
    }

    @GetMapping("/authorize/{provider}")
    public void authorize(@PathVariable("provider") String provider,
                          @RequestParam(value = "tenant", required = false) String tenant,
                          HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(eruptSsoService.authorizeRedirect(provider, tenant, request));
    }

    /**
     * Where the provider sends the browser back. Both outcomes end on the login page:
     * a one-time ticket to exchange, or a message to show.
     */
    @GetMapping({"/callback/{provider}", "/tenant/{tenant}/callback/{provider}"})
    public void callback(@PathVariable("provider") String provider,
                         @PathVariable(value = "tenant", required = false) String tenant,
                         @RequestParam(value = "state", required = false) String state,
                         @RequestParam(value = "code", required = false) String authCode,
                         @RequestParam(value = "error", required = false) String error,
                         HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (null != error) {
            // the provider refused before erupt was ever involved, its own wording is the useful one
            response.sendRedirect(eruptSsoService.errorRedirect(tenant, state, error, request));
            return;
        }
        response.sendRedirect(eruptSsoService.callbackRedirect(provider, tenant, state, authCode, request));
    }

    /**
     * The SAML assertion consumer service: the IdP has the browser POST the response here,
     * with the state coming back as RelayState. Same ending as the GET callback.
     */
    @PostMapping({"/callback/{provider}", "/tenant/{tenant}/callback/{provider}"})
    public void assertionConsumer(@PathVariable("provider") String provider,
                                  @PathVariable(value = "tenant", required = false) String tenant,
                                  @RequestParam(value = "RelayState", required = false) String relayState,
                                  @RequestParam(value = "SAMLResponse", required = false) String samlResponse,
                                  HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(eruptSsoService.callbackRedirect(provider, tenant, relayState, samlResponse, request));
    }

    /**
     * What an IdP imports to learn about this SP: entity id and consumer URL.
     */
    @GetMapping(value = {"/saml/{provider}/metadata", "/tenant/{tenant}/saml/{provider}/metadata"}, produces = "application/samlmetadata+xml;charset=UTF-8")
    public String samlMetadata(@PathVariable("provider") String provider,
                               @PathVariable(value = "tenant", required = false) String tenant,
                               HttpServletRequest request) {
        return eruptSsoService.samlMetadata(provider, tenant, request);
    }

    /**
     * Second half of the redirect: the ticket becomes the session token, over POST,
     * so the token itself never appears in a URL, a history entry or a referrer.
     */
    @PostMapping("/exchange")
    public LoginModel exchange(@RequestBody SsoTicketBody body) {
        LoginModel loginModel = eruptSsoService.consumeTicket(body.getSsoTicket());
        if (null == loginModel) return new LoginModel(false, I18nTranslate.$translate("sso.ticket_expired"));
        return loginModel;
    }

}
