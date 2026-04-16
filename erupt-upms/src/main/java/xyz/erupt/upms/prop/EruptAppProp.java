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

    //Whether to display a watermark
    private boolean waterMark = true;

    //Number of login failures before showing a verification code; 0 means always require a verification code
    private Integer verifyCodeCount = 2;

    //Whether to encrypt the login password during transmission
    private Boolean pwdTransferEncrypt = true;

    //Custom login page path, supports HTTP URL
    private String loginPagePath;

    //Multi-language configuration
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

    //Custom configuration
    private Map<String, Object> properties = new HashMap<>();

    //Toggle for the reset password feature
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

    //Register custom properties
    public void registerProp(String key, Object value) {
        this.properties.put(key, value);
    }

}
