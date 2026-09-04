package xyz.erupt.upms.prop;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.upms.util.IpUtil;

import java.io.File;

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

        //Local path of the ip2region xdb file (v4 or v6); the vector index is cached in memory, segments are read from disk on demand
        private String path = EruptConst.ERUPT_DIR_PATH + File.separator + "ip2region_v4.xdb";

        //Downloaded to `path` on first lookup when the file is missing; set empty to disable auto download
        private String downloadUrl = "https://cdn.jsdelivr.net/gh/lionsoul2014/ip2region@master/data/ip2region_v4.xdb";

    }

}
