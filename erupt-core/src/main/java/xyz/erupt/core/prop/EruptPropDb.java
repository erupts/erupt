package xyz.erupt.core.prop;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;

/**
 * @author YuePeng
 * date 2021/3/28 22:17
 */
@Getter
@Setter
public class EruptPropDb {

    private EruptPropDataSource datasource;

    private JpaProperties jpa;

    private String[] scanPackages;

}
