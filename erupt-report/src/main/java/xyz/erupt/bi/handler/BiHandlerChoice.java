package xyz.erupt.bi.handler;

import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.bi.fun.EruptBiHandler;
import xyz.erupt.core.annotation.EruptHandlerNaming;
import xyz.erupt.core.service.EruptApplication;
import xyz.erupt.core.util.EruptSpringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author YuePeng
 * date 2022/2/20 01:21
 */
public class BiHandlerChoice implements ChoiceFetchHandler {

    private static List<VLModel> biHandlerList;

    @Override
    public synchronized List<VLModel> fetch(String[] params) {
        if (null == biHandlerList) {
            biHandlerList = new ArrayList<>();
            EruptSpringUtil.scannerPackage(EruptApplication.getScanPackage(), new TypeFilter[]{new AssignableTypeFilter(EruptBiHandler.class)}, clazz -> {
                EruptHandlerNaming eruptHandlerNaming = clazz.getAnnotation(EruptHandlerNaming.class);
                biHandlerList.add(new VLModel(clazz.getName(), (null == eruptHandlerNaming) ? clazz.getSimpleName() : eruptHandlerNaming.value()));
            });
        }
        return biHandlerList;
    }
}
