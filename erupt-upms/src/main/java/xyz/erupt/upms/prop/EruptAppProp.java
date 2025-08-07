package xyz.erupt.upms.prop;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

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

    //多语言配置
    private String[] locales = {
            DEFAULT_LANG, // 🇨🇳 简体中文
            "zh-TW",      // 🇭🇰 繁体中文
            "en-US",      // 🇬🇧 English
            "fr-FR",      // 🇫🇷 En français
            "ja-JP",      // 🇯🇵 日本語
            "ko-KR",      // 🇰🇷 한국어
            "ru-RU",      // 🇷🇺 русск
            "es-ES",      // 🇪🇸 español
            "de-DE",      // 🇩🇪 Deutsch
            "pt-PT",      // 🇵🇹 Português
            "id-ID",      // 🇮🇩 Bahasa Indonesia
            "ar-SA",      // 🇸🇦 العربية
    };

    //自定义配置
    private Map<String, Object> properties = new HashMap<>();

    //重置密码功能开关
    private Boolean resetPwd = true;

    private Integer hash;

    private String version;

    public void setLocales(String[] locales) {
        if (null == locales || locales.length == 0) {
            this.locales = new String[]{DEFAULT_LANG};
        } else {
            this.locales = locales;
        }
    }

    //注册自定义属性
    public void registerProp(String key, Object value) {
        this.properties.put(key, value);
    }

}
