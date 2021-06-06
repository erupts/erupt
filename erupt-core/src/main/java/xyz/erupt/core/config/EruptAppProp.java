package xyz.erupt.core.config;

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
@ConfigurationProperties(prefix = "erupt-app")
public class EruptAppProp {

    //登录失败几次出现验证码
    private Integer verifyCodeCount = 2;

    //登录密码是否加密传输
    private Boolean pwdTransferEncrypt = true;

    //多语言配置
    private String[] locale;

}
