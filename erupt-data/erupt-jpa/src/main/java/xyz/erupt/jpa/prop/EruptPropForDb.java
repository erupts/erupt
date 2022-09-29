package xyz.erupt.jpa.prop;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;
import xyz.erupt.jpa.config.HikariCpConfig;

/**
 * @author YuePeng
 * date 2022/9/29 17:59
 */
@Getter
@Setter
@Component
@ConfigurationProperties("erupt")
public class EruptPropForDb {

    //多数据源
    private DB[] dbs;


    @Getter
    @Setter
    public static class DB {

        @NestedConfigurationProperty
        private EruptPropDataSource datasource;

        @NestedConfigurationProperty
        private JpaProperties jpa;

        private String[] scanPackages;

    }

    @Getter
    @Setter
    public static class EruptPropDataSource {

        private String name;

        private String driverClassName;

        private String url;

        private String username;

        private String password;

        @NestedConfigurationProperty
        private HikariCpConfig hikari = new HikariCpConfig();

    }



}
