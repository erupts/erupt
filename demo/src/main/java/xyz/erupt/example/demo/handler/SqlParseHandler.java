package xyz.erupt.example.demo.handler;

import org.dom4j.Element;
import xyz.erupt.eruptapi.handler.SqlHandler;

/**
 * Created by liyuepeng on 2019-08-23.
 */
public class SqlParseHandler implements SqlHandler {
    @Override
    public String handler(Element element, String sql) {
        System.out.println(sql);
        return sql.replaceAll("@uid", "1");
    }
}
