package xyz.erupt.cloud.server.service;

import org.springframework.context.support.StaticApplicationContext;
import xyz.erupt.core.prop.EruptProp;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.util.EruptSpringUtil;

/**
 * The beans the services read through {@link EruptSpringUtil}, which keeps the first context it is given: one
 * context for every test class.
 *
 * @author YuePeng
 */
final class TestContext {

    private TestContext() {
    }

    static void init() {
        if (null != EruptSpringUtil.getApplicationContext()) return;
        StaticApplicationContext context = new StaticApplicationContext();
        // EruptValueValidator reads erupt.choice-validate from EruptProp
        context.registerSingleton(EruptProp.class.getName(), EruptProp.class);
        // the message of a refusal is translated: the key stands for itself
        context.registerBean(I18nTranslate.class, () -> new I18nTranslate() {
            @Override
            public String translate(String key) {
                return key;
            }
        });
        context.refresh();
        new EruptSpringUtil().setApplicationContext(context);
    }

}
