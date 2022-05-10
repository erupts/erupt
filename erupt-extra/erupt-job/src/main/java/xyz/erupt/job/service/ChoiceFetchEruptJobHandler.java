package xyz.erupt.job.service;

import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.core.annotation.EruptHandlerNaming;
import xyz.erupt.core.service.EruptApplication;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.job.handler.EruptJobHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * @author YuePeng
 * date 2021/2/27 22:46
 */
public class ChoiceFetchEruptJobHandler implements ChoiceFetchHandler {

    private static List<VLModel> loadedJobHandler;

    @Override
    public synchronized List<VLModel> fetch(String[] params) {
        if (null == loadedJobHandler) {
            loadedJobHandler = new ArrayList<>();
            EruptSpringUtil.scannerPackage(EruptApplication.getScanPackage(), new TypeFilter[]{new AssignableTypeFilter(EruptJobHandler.class)}, clazz -> {
                EruptHandlerNaming eruptHandlerNaming = clazz.getAnnotation(EruptHandlerNaming.class);
                loadedJobHandler.add(new VLModel(clazz.getName(), (null == eruptHandlerNaming) ? clazz.getSimpleName() : eruptHandlerNaming.value()));
            });
        }
        return loadedJobHandler;
    }

}
