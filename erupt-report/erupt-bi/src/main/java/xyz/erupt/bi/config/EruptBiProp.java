package xyz.erupt.bi.config;

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
public class EruptBiProp {

    // 打印查询日志
    private Boolean queryLog = true;

    // 开启缓存功能
    private Boolean enableCache = true;

    //指定每页可以显示多少条
    private Integer[] pageSizeOptions = {10, 30, 50, 100};

    //每页条数
    private Integer pageSize = 10;

    //后端分页场景下单次查询最大结果集
    private Integer singleMaxResultNum = 500;

}
