package xyz.erupt.report.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author YuePeng
 * date 2021/1/22 10:11
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "erupt.bi")
public class EruptReportProp {

    // enable query logging
    private Boolean queryLog = true;

    // enable cache
    private Boolean enableCache = true;

    // only super-admin users can publish reports
    private Boolean superAdminPublish = false;

    // available page size options
    private Integer[] pageSizeOptions = {10, 30, 50, 100};

    // default page size
    private Integer pageSize = 10;

    // max result set per query for backend pagination
    private Integer singleMaxResultNum = 500;

}
