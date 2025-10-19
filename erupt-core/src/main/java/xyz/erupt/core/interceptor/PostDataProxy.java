package xyz.erupt.core.interceptor;

import lombok.Getter;
import xyz.erupt.annotation.fun.DataProxy;

import java.util.ArrayList;
import java.util.List;

/**
 * @author YuePeng
 * date 2025/10/19 22:06
 */
public class PostDataProxy {

    @Getter
    private static List<Class<? extends DataProxy<Object>>> dataProxies = new ArrayList<>();

    public static void register(Class<? extends DataProxy<Object>> dataProxy) {
        dataProxies.add(dataProxy);
    }

}
