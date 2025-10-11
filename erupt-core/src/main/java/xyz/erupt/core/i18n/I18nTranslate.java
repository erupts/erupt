package xyz.erupt.core.i18n;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.erupt.core.prop.EruptProp;
import xyz.erupt.core.util.EruptSpringUtil;

/**
 * @author YuePeng
 * date 2023/6/23 21:25
 */
@Service
@Slf4j
public class I18nTranslate {

    @Resource
    private HttpServletRequest request;

    @Resource
    private EruptProp eruptProp;

    private static final I18nRunner i18nRunner = new I18nRunner();

    static {
        i18nRunner.init();
    }

    public static String $translate(String key) {
        return EruptSpringUtil.getBean(I18nTranslate.class).translate(key);
    }

    public String translate(String key) {
        String lang = getLang();
        return this.translate(lang == null ? eruptProp.getDefaultLocales() : lang, key);
    }

    public String translate(String lang, String key) {
        return I18nRunner.getI18nValue(lang, key);
    }

    public String getLang() {
        try {
            String lang = request.getHeader("Lang");
            if (I18nRunner.langs().contains(lang)) {
                return lang;
            } else {
                log.warn("Error: Invalid lang: {}", lang);
                return null;
            }
        } catch (Exception ignored) {
            return null;
        }
    }

}
