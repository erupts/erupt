package xyz.erupt.bi.fun;

import java.util.List;
import java.util.Map;

/**
 * @author liyuepeng
 * @date 2019-08-26.
 */
public interface BiHandler {

    /**
     * 表达式处理
     *
     * @param param 参数
     * @param expr  表达式
     * @return
     */
    default String exprHandler(String param, String expr) {

        return expr;
    }

    /**
     * 返回结果处理
     *
     * @param param  参数
     * @param result 结果
     */
    default void resultHandler(String param, List<Map<String, Object>> result) {

    }
}
