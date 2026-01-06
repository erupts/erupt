package xyz.erupt.job.service;

import lombok.SneakyThrows;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.annotation.sub_field.sub_edit.OnChange;
import xyz.erupt.core.annotation.EruptHandlerNaming;
import xyz.erupt.core.service.EruptApplication;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.job.handler.EruptJobHandler;
import xyz.erupt.job.model.EruptJob;
import xyz.erupt.linq.lambda.LambdaSee;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author YuePeng
 * date 2021/2/27 22:46
 */
@Component
public class EruptJobFetch implements ChoiceFetchHandler, OnChange<EruptJob> {

    private List<VLModel> loadedJobHandler;

    public void init() {
        loadedJobHandler = new ArrayList<>();
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
        if (null == loadedJobHandler) {
            init();
        }
        return loadedJobHandler;
    }

    @SneakyThrows
    @Override
    public Map<String, Object> populateForm(EruptJob eruptJob, String[] params) {
        if (null != eruptJob.getHandler()) {
            for (VLModel vl : loadedJobHandler) {
                if (vl.getValue().equals(eruptJob.getHandler())) {
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
        }
        return Map.of();
    }

    @Override
    public Map<String, String> buildEditExpr(EruptJob eruptJob, String[] params) {
        return Map.of();
    }
}
