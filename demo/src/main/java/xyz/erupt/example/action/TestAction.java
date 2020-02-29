package xyz.erupt.example.action;

import org.springframework.stereotype.Service;
import xyz.erupt.tpl.annotation.EruptTpl;
import xyz.erupt.tpl.annotation.TplAction;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author liyuepeng
 * @date 2020-02-24
 */
@EruptTpl(engine = EruptTpl.Engine.FreeMarker)
@Service
public class TestAction {

    @TplAction("dashboard.ftl")
    public Map<String, Object> dashboard() {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> mp = new LinkedHashMap<>();
        mp.put("annotation", 'E');
        mp.put("core", 'R');
        mp.put("tool", 'U');
        mp.put("auth", 'P');
        mp.put("web", 'T');
        mp.put("bi", '-');
        mp.put("scheduler", '-');
        mp.put("tpl", '-');
        map.put("color", new String[]{
                "#eb776e", "#56aad6", "#69d5e7", "#f686e5", "#29ae94", "#fbd364",
                "#4da1ff", "#ff6e4b", "#ffc524", "#e07de9", "#42e9e1", "#a9f", "#a90",
                "#09f", "#928bff"
        });
        map.put("map", mp);
        return map;
    }

}
