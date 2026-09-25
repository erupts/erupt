package xyz.erupt.sso.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * What the login page needs to draw one sign-on button. Configuration stays on the server.
 *
 * @author YuePeng
 * date 2026-09-18
 */
@Getter
@Setter
@AllArgsConstructor
public class EruptSsoProviderVo {

    private String code;

    private String name;

    private String icon;

}
