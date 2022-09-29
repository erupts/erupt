package xyz.erupt.upms.prop;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author YuePeng
 * date 2019-10-31.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "erupt.upms", ignoreUnknownFields = false)
public class EruptUpmsProp {

    //redis session时长
    private Integer expireTimeByLogin = 100;

    //严格的角色菜单策略，如果非管理员用户拥有角色权限则仅能编辑已有权限的菜单
    private boolean strictRoleMenuLegal = true;

}
