package xyz.erupt.example.handler;

import xyz.erupt.annotation.sub_erupt.Html;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liyuepeng
 * @date 2019-10-18.
 */
public class HtmlHandler implements Html.HtmlHandler {

    @Override
    public Map<String, Object> getData(String[] params) {
        Map<String, Object> map = new HashMap<>();
        map.put("number", 123);
        map.put("str", "ERUPT DEMO HTML");
        map.put("arr", new String[]{
                "a1", "a2", "a3"
        });
        return map;
    }
}
