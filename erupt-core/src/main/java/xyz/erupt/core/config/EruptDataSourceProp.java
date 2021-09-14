package xyz.erupt.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * @author YuePeng
 * date 2021/9/14
 */
@Getter
@Setter
public class EruptDataSourceProp {

    private String name;

    private String driverClassName;

    private String url;

    private String username;

    private String password;

//    @NestedConfigurationProperty
//    private DruidConfig druid = new DruidConfig();
//    @NestedConfigurationProperty
//    private HikariCpConfig hikari = new HikariCpConfig();

}
