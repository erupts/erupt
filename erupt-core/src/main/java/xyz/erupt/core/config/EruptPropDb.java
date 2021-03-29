package xyz.erupt.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;

/**
 * @author YuePeng
 * date 2021/3/28 22:17
 */
@Getter
@Setter
public class EruptPropDb {

    private DataSourceProperties datasource;

    private JpaProperties jpa;

}
