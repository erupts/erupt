package xyz.erupt.core.module;

import xyz.erupt.core.util.EruptSpringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author YuePeng
 * date 2022/1/4 21:58
 */
public class EruptModuleInvoke {

    private static final List<Class<? extends EruptModule>> ERUPT_MODULES = new ArrayList<>();

    public static void addEruptModule(Class<? extends EruptModule> eruptModule) {
        ERUPT_MODULES.add(eruptModule);
    }

    public static void invoke(Consumer<EruptModule> consumer) {
        ERUPT_MODULES.forEach(it -> consumer.accept(EruptSpringUtil.getBean(it)));
    }

}
