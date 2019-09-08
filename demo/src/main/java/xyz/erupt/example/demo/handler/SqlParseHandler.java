package xyz.erupt.example.demo.handler;

import org.dom4j.Element;
import org.springframework.stereotype.Component;
import xyz.erupt.erupt_open_api.handler.SqlHandler;

import java.util.Map;

/**
 * Created by liyuepeng on 2019-08-23.
 */
@Component
public class SqlParseHandler implements SqlHandler {
    @Override
    public String handler(Element element, String sql, Map<String, Object> param) {
        System.out.println(sql);
        param.put("xxx", 23333);
        return sql.replaceAll("@uid", "1");
    }

    @Override
    public Object handlerResult(Element element, Object result, Map<String, Object> param) {
        return result;
    }
}
