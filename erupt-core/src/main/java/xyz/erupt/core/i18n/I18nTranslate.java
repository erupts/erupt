package xyz.erupt.core.i18n;

import org.springframework.stereotype.Service;
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

    public static String $translate(String key) {
        return EruptSpringUtil.getBean(I18nTranslate.class).translate(key);
    }

    public String translate(String key) {
        String lang = getLang();
        if (null != lang) {
            return I18nRunner.getI18nValue(lang, key);
        }
        return key;
    }

    public String getLang() {
        try {
            return request.getHeader("lang");
        } catch (Exception ignored) {
            return null;
        }
    }

}
