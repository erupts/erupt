package xyz.erupt.core.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * 简单的翻译服务
 *
 * @author YuePeng
 * date 2021/10/29 21:38
 */
@Service
public class I18NTranslateService {

    @Resource
    private HttpServletRequest request;

    private final Map<String, Properties> i18NMapping = new HashMap<>();

    public void registerI18NMapping(Map<String, Properties> props) {
        i18NMapping.putAll(props);
    }

    public boolean isZh() {
        String lang = request.getHeader("lang");
        if (StringUtils.isNotBlank(lang)) {
            return lang.toLowerCase(Locale.ROOT).startsWith("zh");
        } else {
            return true;
        }
    }

    public String translate(String key, Object... args) {
        String lang = request.getHeader("lang");
        if (null != lang) {
            lang = lang.toLowerCase();
            if (i18NMapping.containsKey(lang)) {
                String result = i18NMapping.get(lang).getProperty(key);
                if (StringUtils.isNotBlank(result)) {
                    return String.format(result, args);
                }
            }
        }

        return key;
    }

}
