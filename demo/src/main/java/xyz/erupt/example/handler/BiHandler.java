package xyz.erupt.example.handler;

import java.util.List;
import java.util.Map;

/**
 * @author liyuepeng
 * @date 2020-04-20
 */
public class BiHandler implements xyz.erupt.bi.fun.BiHandler {
    @Override
    public String exprHandler(String param, String expr) {
        return expr;
    }

    @Override
    public void resultHandler(String param, List<Map<String, Object>> result) {
        System.out.println(result);
    }
}
