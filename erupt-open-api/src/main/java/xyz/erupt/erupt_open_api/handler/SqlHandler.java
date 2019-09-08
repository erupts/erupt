package xyz.erupt.erupt_open_api.handler;

import org.dom4j.Element;

import java.util.Map;


/**
 * Created by liyuepeng on 2019-08-15.
 */
public interface SqlHandler {

    String handler(Element element, String sql, Map<String, Object> param);

    default Object handlerResult(Element element, Object result, Map<String, Object> param) {
        return result;
    }
}
