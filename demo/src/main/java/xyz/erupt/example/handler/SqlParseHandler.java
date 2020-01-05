//package xyz.erupt.example.demo.filter;
//
//import org.dom4j.Element;
//import org.springframework.stereotype.Component;
//import xyz.erupt.erupt_open_api.filter.SqlHandler;
//
//import java.util.Map;
//
///**
// * @author liyuepeng
// * @date 2019-08-23.
// */
//@Component
//public class SqlParseHandler implements SqlHandler {
//    @Override
//    public String filter(Element element, String sql, Map<String, Object> param) {
//        System.out.println(sql);
//        param.put("xxx", 23333);
//        return sql.replaceAll("@uid", "1");
//    }
//
//    @Override
//    public Object handlerResult(Element element, Object result, Map<String, Object> param) {
//        return result;
//    }
//}
