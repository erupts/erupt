package xyz.erupt.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author liyuepeng
 * @date 2021/1/22 10:11
 */
@Data
@Component
@ConfigurationProperties(prefix = "erupt-app")
public class EruptAppProp {

    //登录失败几次出现验证码
    private Integer verifyCodeCount = 2;

}
