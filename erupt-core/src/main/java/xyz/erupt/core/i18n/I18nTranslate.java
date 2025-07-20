package xyz.erupt.core.i18n;

import org.springframework.stereotype.Service;
import xyz.erupt.core.prop.EruptProp;
import xyz.erupt.core.util.EruptSpringUtil;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author YuePeng
 * date 2023/6/23 21:25
 */
@Service
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
            return request.getHeader("lang");
        } catch (Exception ignored) {
            return null;
        }
    }

}
