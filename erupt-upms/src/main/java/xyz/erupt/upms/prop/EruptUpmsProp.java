package xyz.erupt.upms.prop;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.upms.util.IpUtil;


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

    //IP -> region lookup (login / operation logs)
    private Ip2Region ip2region = new Ip2Region();

    @PostConstruct
    public void init() {
        IpUtil.init(ip2region);
    }

    @Getter
    @Setter
    public static class Ip2Region {

        //Disable to skip region lookup entirely
        private boolean enable = true;

        //Optional external xdb (v4 or v6) that overrides the copy bundled in erupt-upms.
        //Point it at a newer or a v6 database when the bundled one is too old; only its
        //vector index is held in memory and segments are read from disk per lookup.
        //Empty means use the bundled v4 database, which needs no setup and no network.
        private String path = "";

    }

}
