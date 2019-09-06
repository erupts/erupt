package xyz.erupt.example.demo.handler;

import org.dom4j.Element;
import org.json.JSONObject;
import xyz.erupt.eruptapi.handler.SqlHandler;

import javax.persistence.Query;

/**
 * Created by liyuepeng on 2019-08-23.
 */
public class SqlParseHandler implements SqlHandler {
    @Override
    public String handler(Element element, String sql) {
        System.out.println(sql);
        return sql.replaceAll("@uid", "1");
    }

    @Override
    public Object handlerResult(Element element, Query query, Object result) {
        boolean bool = (boolean) result;
        if (bool) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("a", 223333);
            jsonObject.put("b", true);
            return jsonObject.toString();
        } else {
            return result;
        }
    }
}
