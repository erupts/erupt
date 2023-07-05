package xyz.erupt.upms.prop;

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

    //是否显示水印
    private boolean waterMark = true;

    //登录失败几次出现验证码，0表示一直要求输入验证码
    private Integer verifyCodeCount = 2;

    //登录密码是否加密传输
    private Boolean pwdTransferEncrypt = true;

    //自定义登录页路径，支持http网络路径
    private String loginPagePath;

    private Integer hash;

    private String version;

    //多语言配置
    private String[] locales = {
            DEFAULT_LANG, // 简体中文
            "zh-TW",      // 繁体中文
            "en-US",      // English
            "fr-FR",      // En français
            "ja-JP",      // 日本語
            "ko-KR",      // 한국어
            "ru_RU",      // русск
            "es_ES"       // español
    };

}
