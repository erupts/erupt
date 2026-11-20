package xyz.erupt.upms.service;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import xyz.erupt.upms.base.LoginModel;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.spi.LoginPolicy;

/**
 * Maintenance mode of the site settings at sign-in: a session is issued to administrators only,
 * everyone else is refused with the maintenance text. Sessions already open are cut by the security
 * interceptor at their next request. Checked before a token is minted, so every login path (password,
 * MFA, single sign-on) passes it; a refusal is a verdict, not a credential failure.
 *
 * @author YuePeng
 * date 2026/11/21
 */
@Component
public class MaintenanceLoginPolicy implements LoginPolicy {

    @Resource
    private EruptSiteSettingService eruptSiteSettingService;

    @Override
    public String beforeIssueToken(EruptUser eruptUser, LoginModel loginModel, HttpServletRequest request) {
        if (!eruptSiteSettingService.maintenanceActive() || Boolean.TRUE.equals(eruptUser.getIsAdmin())) return null;
        return eruptSiteSettingService.maintenanceMessage();
    }

}
