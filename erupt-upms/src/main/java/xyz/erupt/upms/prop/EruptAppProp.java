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

    /** Whether to display a watermark */
    private boolean waterMark = true;

    /** Whether the watermark displays the date */
    private boolean waterMarkDate = false;

    /** Custom watermark content */
    private String waterMarkContent = null;

    /** Number of login failures before showing a captcha; 0 means always require one */
    private Integer verifyCodeCount = 2;

    /** Whether the login password is encoded during transmission */
    private Boolean pwdTransferEncrypt = true;

    /** Custom login page path, supports an HTTP URL */
    private String loginPagePath;

    /** Languages offered by the UI, as locale tags */
    private String[] locales = {
            "en-US",      // 🇬🇧 English
            "fr-FR",      // 🇫🇷 En français
            "de-DE",      // 🇩🇪 Deutsch
            "zh-CN",      // 🇨🇳 Simplified Chinese
            "zh-TW",      // 🇭🇰 Traditional Chinese
            "ja-JP",      // 🇯🇵 Japanese
            "ko-KR",      // 🇰🇷 Korean
            "ru-RU",      // 🇷🇺 русск
            "es-ES",      // 🇪🇸 español
            "pt-PT",      // 🇵🇹 Português
            "id-ID",      // 🇮🇩 Bahasa Indonesia
            "ar-SA",      // 🇸🇦 العربية
    };

    /** Custom configuration handed to the frontend */
    private Map<String, Object> properties = new HashMap<>();

    /** Multi-factor authentication (TOTP) */
    private Mfa mfa = new Mfa();

    /** Toggle for the reset password feature */
    private Boolean resetPwd = true;

    /** Whether to prompt users who still use the default password */
    private Boolean resetPwdPrompt = false;

    /** Changes on every server restart */
    private Integer hash;

    /** Erupt version */
    private String version;

    /** Domain that hosts uploaded attachments, from the registered AttachmentProxy; null when erupt serves them itself */
    private String fileDomain;

    public void setLocales(String[] locales) {
        if (null == locales || locales.length == 0) {
            this.locales = new String[]{"en-US"};
        } else {
            this.locales = locales;
        }
    }

    //Register custom properties
    public void registerProp(String key, Object value) {
        this.properties.put(key, value);
    }

    @Getter
    @Setter
    public static class Mfa {

        /** Whether users may protect their account with a TOTP authenticator. On by default: it only opens the feature, it does not force anyone to enrol, so an upgrade never locks an existing user out */
        private boolean enable = true;

        /** Name shown by the authenticator app; falls back to the application name when blank */
        private String issuer = "";

        /** How many 30 second steps either side of now are accepted, to absorb clock drift */
        private int window = 1;

    }

}
