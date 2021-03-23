package xyz.erupt.core.invoke;

import xyz.erupt.core.annotation.EruptDataProcessor;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.service.IEruptDataService;
import xyz.erupt.core.util.EruptSpringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author YuePeng
 * date 2020/12/4 16:24
 */
public class DataProcessorManager {

    private static final Map<String, Class<? extends IEruptDataService>> eruptDataServiceMap = new HashMap<>();

    public static void register(String name, Class<? extends IEruptDataService> eruptDataService) {
        eruptDataServiceMap.put(name, eruptDataService);
    }

    public static IEruptDataService getEruptDataProcessor(Class<?> clazz) {
        EruptDataProcessor eruptDataProcessor = clazz.getAnnotation(EruptDataProcessor.class);
        if (null == eruptDataProcessor) {
            return EruptSpringUtil.getBean(eruptDataServiceMap.get(EruptConst.DEFAULT_DATA_PROCESSOR));
        } else {
            return EruptSpringUtil.getBean(eruptDataServiceMap.get(eruptDataProcessor.value()));
        }
    }
}
