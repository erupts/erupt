package xyz.erupt.core.i18n;

import org.springframework.stereotype.Service;
import xyz.erupt.core.config.EruptAppProp;

import javax.annotation.Resource;
import java.util.Locale;

/**
 * @author YuePeng
 * date 2021/5/25 18:26
 */
@Service
public class EruptI18nService {

    @Resource
    private EruptAppProp eruptAppProp;

    public String getMessage(Locale locale) {

        return null;
    }

}
