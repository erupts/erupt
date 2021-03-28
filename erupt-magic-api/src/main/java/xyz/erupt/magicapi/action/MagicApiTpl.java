package xyz.erupt.magicapi.action;

import org.springframework.stereotype.Component;
import org.ssssssss.magicapi.spring.boot.starter.MagicAPIProperties;
import xyz.erupt.tpl.annotation.EruptTpl;
import xyz.erupt.tpl.annotation.TplAction;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author YuePeng
 * date 2021/3/28 15:56
 */
@EruptTpl
@Component
public class MagicApiTpl {

    public static final String MAGIC_API_PERMISSION = "magic-api.ftl";

    @Resource
    private MagicAPIProperties magicAPIProperties;

    @TplAction(MAGIC_API_PERMISSION)
    public Map<String, Object> magicApiAction() {
        Map<String, Object> map = new HashMap<>();
        map.put("web", magicAPIProperties.getWeb());
        return map;
    }

}
