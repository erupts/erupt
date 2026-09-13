package xyz.erupt.core.i18n;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.erupt.core.constant.EruptMutualConst;
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
        return this.translate(this.currentLang(), key);
    }

    /**
     * Effective language of the current request: the "Lang" header when it names a loaded
     * language, otherwise the configured default. Resolve it once when translating many keys.
     */
    public String currentLang() {
        String lang = getLang();
        return lang == null ? eruptProp.getDefaultLocales() : lang;
    }

    public String translate(String lang, String key) {
        return I18nRunner.getI18nValue(lang, key);
    }

    /**
     * Header first, then the {@code _lang} URL parameter: a tpl page is opened as a document,
     * so the admin can only pass its language on the URL. Unknown languages fall through to null
     * and the caller settles on the configured default.
     */
    public String getLang() {
        try {
            String lang = request.getHeader(EruptMutualConst.LANG);
            if (!I18nRunner.langs().contains(lang)) {
                lang = request.getParameter(EruptMutualConst.URL_PARAM_LANG);
            }
            return I18nRunner.langs().contains(lang) ? lang : null;
        } catch (Exception ignored) {
            return null;
        }
    }

}
