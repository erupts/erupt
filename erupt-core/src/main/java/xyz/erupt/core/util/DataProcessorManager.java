package xyz.erupt.core.util;

import xyz.erupt.core.service.IEruptDataService;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liyuepeng
 * @date 2020/12/4 16:24
 */
public class DataProcessorManager {

    private static final Map<String, Class<? extends IEruptDataService>> eruptDataServiceMap = new HashMap<>();

    public static void register(String name, Class<? extends IEruptDataService> eruptDataService) {
        eruptDataServiceMap.put(name, eruptDataService);
    }

    public static Class<? extends IEruptDataService> getDataProcessor(String name) {
        return eruptDataServiceMap.get(name);
    }
}
