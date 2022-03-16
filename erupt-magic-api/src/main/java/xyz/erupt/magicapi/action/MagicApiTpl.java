package xyz.erupt.magicapi.action;

import org.springframework.stereotype.Component;
import org.ssssssss.magicapi.core.config.MagicAPIProperties;
import xyz.erupt.core.util.EruptInformation;
import xyz.erupt.tpl.annotation.EruptTpl;
import xyz.erupt.tpl.annotation.TplAction;
import xyz.erupt.upms.service.EruptContextService;

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

    @Resource
    private EruptContextService eruptContextService;

    @TplAction(MAGIC_API_PERMISSION)
    public Map<String, Object> magicApiAction() {
        Map<String, Object> map = new HashMap<>();
        map.put("web", magicAPIProperties.getWeb());
        map.put("token", eruptContextService.getCurrentToken());
        map.put("v", EruptInformation.getEruptVersion());
        map.put("hash", this.hashCode());
        return map;
    }

}
