package xyz.erupt.ai.handler;

import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.core.service.EruptApplication;
import xyz.erupt.core.util.EruptSpringUtil;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * @author YuePeng
 * date 2025/3/1 19:16
 */
@Component
public class DynamicPromptFetch implements ChoiceFetchHandler {

    private static final List<VLModel> promptHandlers = new ArrayList<>();

    @PostConstruct
    public void init() {
        EruptSpringUtil.scannerPackage(EruptApplication.getScanPackage(), new TypeFilter[]{new AssignableTypeFilter(EruptPromptHandler.class)},
                clazz -> promptHandlers.add(new VLModel(clazz.getName(), clazz.getName()))
        );
    }

    @Override
    public List<VLModel> fetch(String[] params) {
        return promptHandlers;
    }

}
