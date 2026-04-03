package xyz.erupt.annotation.fun;

import xyz.erupt.annotation.config.Comment;

import java.util.List;
import java.util.Map;

public interface CubeProxy {

    @Comment("查询表达式动态处理")
    default String beforeQuery(String expr, Map<String, Object> context) {
        return expr;
    }


    @Comment("返回结果处理")
    default void afterQuery(List<CubeResultRow> result, Map<String, Object> context) {

    }

}
