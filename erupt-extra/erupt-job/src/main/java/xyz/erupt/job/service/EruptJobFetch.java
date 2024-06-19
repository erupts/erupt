package xyz.erupt.job.service;

import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.ChoiceTrigger;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.core.annotation.EruptHandlerNaming;
import xyz.erupt.core.service.EruptApplication;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.job.handler.EruptJobHandler;
import xyz.erupt.job.model.EruptJob;
import xyz.erupt.linq.lambda.LambdaSee;

import java.util.*;

/**
 * @author YuePeng
 * date 2021/2/27 22:46
 */
public class EruptJobFetch implements ChoiceFetchHandler, ChoiceTrigger {

    private static List<VLModel> loadedJobHandler;

    @Override
    public synchronized List<VLModel> fetch(String[] params) {
        if (null == loadedJobHandler) {
            loadedJobHandler = new ArrayList<>();
            EruptSpringUtil.scannerPackage(EruptApplication.getScanPackage(), new TypeFilter[]{new AssignableTypeFilter(EruptJobHandler.class)}, clazz -> {
                EruptHandlerNaming eruptHandlerNaming = clazz.getAnnotation(EruptHandlerNaming.class);
                if (null == eruptHandlerNaming) {
                    loadedJobHandler.add(new VLModel(clazz.getName(), ((EruptJobHandler) EruptSpringUtil.getBean(clazz)).name()));
                } else {
                    loadedJobHandler.add(new VLModel(clazz.getName(), eruptHandlerNaming.value()));
                }
            });
        }
        return loadedJobHandler;
    }

    @Override
    public Map<String, Object> trigger(Object value, String[] params) {
        for (VLModel vl : loadedJobHandler) {
            if (vl.getValue().equals(value)) {
                Map<String, Object> map = new HashMap<>();
                map.put(LambdaSee.field(EruptJob::getHandlerParam), null);
                map.put(LambdaSee.field(EruptJob::getCron), null);
                return map;
            }
        }
        return Collections.emptyMap();
    }
}
