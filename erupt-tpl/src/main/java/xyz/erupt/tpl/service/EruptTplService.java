package xyz.erupt.tpl.service;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedCaseInsensitiveMap;
import xyz.erupt.core.config.EruptProp;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.tpl.annotation.EruptTpl;
import xyz.erupt.tpl.annotation.TplAction;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author liyuepeng
 * @date 2020-02-24
 */
@Order(1)
@Service
@Log
public class EruptTplService implements ApplicationRunner {

    @Autowired
    private EruptProp eruptProp;

    private final Map<String, Method> tplActions = new LinkedCaseInsensitiveMap<>();

    public Method getAction(String name) {
        return tplActions.get(name);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        EruptSpringUtil.scannerPackage(eruptProp.getScannerPackage(), new TypeFilter[]{
                new AnnotationTypeFilter(EruptTpl.class)
        }, clazz -> {
            for (Method method : clazz.getDeclaredMethods()) {
                TplAction tplAction = method.getAnnotation(TplAction.class);
                if (null != tplAction) {
                    tplActions.put(tplAction.value(), method);
                }
            }
        });
        log.info("Erupt tpl initialization complete");
    }
}
