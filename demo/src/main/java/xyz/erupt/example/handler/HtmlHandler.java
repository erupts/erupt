package xyz.erupt.example.handler;

import xyz.erupt.annotation.sub_erupt.Tpl;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liyuepeng
 * @date 2019-10-18.
 */
public class HtmlHandler implements Tpl.TplHandler {

    @Override
    public Map<String, Object> tplAction(String[] params) {
        Map<String, Object> map = new HashMap<>();
        map.put("number", 123);
        map.put("str", "ERUPT-TPL");
        map.put("arr", new String[]{
                "a1", "a2", "a3"
        });
        return map;
    }

}
