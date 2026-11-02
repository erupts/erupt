package xyz.erupt.test.sso;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xyz.erupt.upms.base.LoginModel;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.spi.UpmsTenantLogin;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * What erupt-tenant contributes, reduced to the answers {@link SsoTenantLoginTest} needs. It lives in the
 * one application context every test shares: a test configuration of its own would start a second context
 * in the same JVM, and erupt's static registries (tool beans, the Spring context holder, the erupt models)
 * then point at whichever context started last, so later tests act on beans of the wrong context.
 */
@Configuration
public class StandInTenantModule {

    static final String TENANT_CODE = "acme-sso";

    static final String TENANT_ID = "t-sso-test";

    static final List<EruptUser> LOGINS = new CopyOnWriteArrayList<>();

    static volatile String refusal;

    @Bean
    UpmsTenantLogin standInTenantLogin() {
        return new UpmsTenantLogin() {
            @Override
            public String tenantId(String tenantCode) {
                return TENANT_CODE.equals(tenantCode) ? TENANT_ID : null;
            }

            @Override
            public LoginModel login(String tenantId, EruptUser user) {
                if (!TENANT_ID.equals(tenantId)) throw new IllegalStateException("login of tenant " + tenantId);
                if (null != refusal) return new LoginModel(false, refusal);
                LOGINS.add(user);
                LoginModel model = new LoginModel(true, user);
                model.setToken("t_stand_in_" + user.getId());
                model.setAccount(user.getAccount());
                return model;
            }

            @Override
            public String loginPage(String tenantCode) {
                return "/#/passport/tenant?tenantCode=" + tenantCode;
            }
        };
    }

}
