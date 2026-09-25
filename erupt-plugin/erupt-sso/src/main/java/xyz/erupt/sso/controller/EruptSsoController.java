package xyz.erupt.sso.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Map;

/**
 * Sign-on endpoints. All four are anonymous by design: they are what a user without a
 * session has to reach, and each one carries its own proof — a state, a ticket or nothing
 * worth protecting.
 *
 * @author YuePeng
 * date 2026-09-18
 */
@RestController
@RequestMapping(EruptRestPath.ERUPT_API + "/sso")
@Slf4j
public class EruptSsoController {

    @Resource
    private EruptSsoService eruptSsoService;

    @GetMapping("/providers")
    public List<EruptSsoProviderVo> providers() {
        return eruptSsoService.providers();
    }

    @GetMapping("/authorize/{provider}")
    public void authorize(@PathVariable("provider") String provider, HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            response.sendRedirect(eruptSsoService.authorizeUrl(provider, request));
        } catch (Exception e) {
            log.warn("sso authorize failed: {}", provider, e);
            response.sendRedirect(eruptSsoService.loginPageUrl(request, Map.of("ssoError", message(e))));
        }
    }

    /**
     * Where the provider sends the browser back. Both outcomes end on the login page:
     * a one-time ticket to exchange, or a message to show.
     */
    @GetMapping("/callback/{provider}")
    public void callback(@PathVariable("provider") String provider,
                         @RequestParam(value = "state", required = false) String state,
                         @RequestParam(value = "code", required = false) String authCode,
                         @RequestParam(value = "error", required = false) String error,
                         HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, String> params;
        if (null != error) {
            // the provider refused before erupt was ever involved, its own wording is the useful one
            params = Map.of("ssoError", error);
        } else {
            try {
                params = Map.of("ssoTicket", eruptSsoService.callback(provider, state, authCode, request));
            } catch (Exception e) {
                log.warn("sso callback failed: {}", provider, e);
                params = Map.of("ssoError", message(e));
            }
        }
        response.sendRedirect(eruptSsoService.loginPageUrl(request, params));
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

    private static String message(Exception e) {
        return null == e.getMessage() ? I18nTranslate.$translate("sso.login_failed") : e.getMessage();
    }

}
