package xyz.erupt.upms.prop;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import xyz.erupt.core.constant.EruptConst;

/**
 * @author YuePeng
 * date 2019-10-31.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "erupt.upms", ignoreUnknownFields = false)
public class EruptUpmsProp {

    //login session Duration (minutes)
    private Integer expireTimeByLogin = 100;

    //Strict role-menu policy: non-admin users with role permissions can only edit menus they already have access to
    private boolean strictRoleMenuLegal = true;

    //default account
    private String defaultAccount = EruptConst.ERUPT;

    //default password
    private String defaultPassword = EruptConst.ERUPT;

}
