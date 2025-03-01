package xyz.erupt.job.service;

import lombok.SneakyThrows;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.ChoiceTrigger;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.core.annotation.EruptHandlerNaming;
import xyz.erupt.core.service.EruptApplication;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.job.handler.EruptJobHandler;
import xyz.erupt.job.model.EruptJob;
import xyz.erupt.linq.lambda.LambdaSee;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * @author YuePeng
 * date 2021/2/27 22:46
 */
@Component
public class EruptJobFetch implements ChoiceFetchHandler, ChoiceTrigger {

    private static final List<VLModel> loadedJobHandler = new ArrayList<>();

    @PostConstruct
    public void init() {
        EruptSpringUtil.scannerPackage(EruptApplication.getScanPackage(), new TypeFilter[]{new AssignableTypeFilter(EruptJobHandler.class)}, clazz -> {
            EruptHandlerNaming eruptHandlerNaming = clazz.getAnnotation(EruptHandlerNaming.class);
            if (null == eruptHandlerNaming) {
                loadedJobHandler.add(new VLModel(clazz.getName(), ((EruptJobHandler) EruptSpringUtil.getBean(clazz)).name(), clazz.getName()));
            } else {
                loadedJobHandler.add(new VLModel(clazz.getName(), eruptHandlerNaming.value(), clazz.getName()));
            }
        });
    }

    @Override
    public synchronized List<VLModel> fetch(String[] params) {
        return loadedJobHandler;
    }

    @Override
    @SneakyThrows
    public Map<String, Object> trigger(Object value, String[] params) {
        for (VLModel vl : loadedJobHandler) {
            if (vl.getValue().equals(value)) {
                Map<String, Object> map = new HashMap<>();
                EruptJobHandler jobHandler = EruptSpringUtil.getBeanByPath(vl.getDesc(), EruptJobHandler.class);
                map.put(LambdaSee.field(EruptJob::getName), vl.getLabel());
                if (null != jobHandler.param()) {
                    map.put(LambdaSee.field(EruptJob::getHandlerParam), jobHandler.param());
                }
                if (null != jobHandler.cron()) {
                    map.put(LambdaSee.field(EruptJob::getCron), jobHandler.cron());
                }
                return map;
            }
        }
        return Collections.emptyMap();
    }
}
