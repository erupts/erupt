package xyz.erupt.example.action;

import org.springframework.stereotype.Service;
import xyz.erupt.tpl.annotation.EruptTpl;
import xyz.erupt.tpl.annotation.TplAction;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liyuepeng
 * @date 2020-02-24
 */
@EruptTpl
@Service
public class Test2Action {

    @TplAction("dashboard2")
    public Map<String, Object> dashboard2() {
        Map<String, Object> map = new HashMap<>();
        map.put("list", new String[]{
                "E", "R", "U", "P", "T"
        });
        map.put("str", "aaaaa");
        return map;
    }

}
