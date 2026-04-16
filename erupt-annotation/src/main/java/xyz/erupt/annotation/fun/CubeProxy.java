package xyz.erupt.annotation.fun;

import xyz.erupt.annotation.config.Comment;

import java.util.List;
import java.util.Map;

public interface CubeProxy {

    @Comment("Dynamic processing of query expressions")
    default String beforeQuery(String expr, Map<String, Object> context) {
        return expr;
    }


    @Comment("Post-query result processing")
    default void afterQuery(List<CubeResultRow> result, Map<String, Object> context) {

    }

}
