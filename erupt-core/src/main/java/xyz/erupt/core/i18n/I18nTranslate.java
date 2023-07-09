package xyz.erupt.core.i18n;

import org.apache.commons.lang3.StringUtils;
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

    public static String $translate(String key, Object... args) {
        return EruptSpringUtil.getBean(I18nTranslate.class).translate(key, args);
    }

    public String translate(String key, Object... args) {
        String lang = getLang();
        if (null != lang) {
            String result = I18nRunner.getI18nValue(lang, key);
            if (StringUtils.isNotBlank(result)) {
                return String.format(result, args);
            }
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
