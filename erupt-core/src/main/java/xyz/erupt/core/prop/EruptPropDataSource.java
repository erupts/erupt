package xyz.erupt.core.prop;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import xyz.erupt.core.config.HikariCpConfig;

/**
 * @author YuePeng
 * date 2021/9/14
 */
@Getter
@Setter
public class EruptPropDataSource {

    private String name;

    private String driverClassName;

    private String url;

    private String username;

    private String password;

    @NestedConfigurationProperty
    private HikariCpConfig hikari = new HikariCpConfig();

}
