package xyz.erupt.upms.handler;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.expr.ExprBool;
import xyz.erupt.core.util.EruptAssert;
import xyz.erupt.upms.service.EruptUserService;

/**
 * @author YuePeng
 * date 2020/12/28 22:33
 */
@Service
@Comment("Controls visibility based on menu type value")
public class ViaMenuValueCtrl implements ExprBool.ExprHandler {

    @Resource
    private EruptUserService eruptUserService;

    @Override
    @Comment("params is required; the value is the menu type value")
    public boolean handler(boolean expr, String[] params) {
        EruptAssert.notNull(params,ViaMenuValueCtrl.class.getSimpleName() + " → params[0] not found");
        return null != eruptUserService.getEruptMenuByValue(params[0]);
    }

}
