package xyz.erupt.core.prop;

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

    public static final String DEFAULT_LANG = "zh-CN";

    //登录失败几次出现验证码
    private Integer verifyCodeCount = 2;

    //登录密码是否加密传输
    private Boolean pwdTransferEncrypt = true;

    //多语言配置
    private String[] locales = {DEFAULT_LANG,
            "zh-TW",  //繁体中文
            "en-US",  //英文
            "ja-JP",  //日文
            "ko-KR"   //韩文
    };

}
