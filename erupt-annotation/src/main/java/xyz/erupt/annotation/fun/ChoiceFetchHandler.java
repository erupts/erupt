package xyz.erupt.annotation.fun;

import java.util.Map;

/**
 * @author liyuepeng
 * @date 2019-07-25.
 */
public interface ChoiceFetchHandler {

    /**
     * 动态获取choice数据
     *
     * @param params 注解参数
     * @return 前端显示的键值对
     */
    Map<String, String> fetch(String[] params);
}
