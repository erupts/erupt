package xyz.erupt.upms.handler;

import org.springframework.stereotype.Service;
import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.expr.ExprBool;
import xyz.erupt.core.util.EruptAssert;
import xyz.erupt.upms.service.EruptUserService;

import jakarta.annotation.Resource;

/**
 * @author YuePeng
 * date 2020/12/28 22:33
 */
@Service
@Comment("通过菜单类型值控制是否显示")
public class ViaMenuValueCtrl implements ExprBool.ExprHandler {

    @Resource
    private EruptUserService eruptUserService;

    @Override
    @Comment("params必填，值为菜单类型值")
    public boolean handler(boolean expr, String[] params) {
        EruptAssert.notNull(params,ViaMenuValueCtrl.class.getSimpleName() + " → params[0] not found");
        return null != eruptUserService.getEruptMenuByValue(params[0]);
    }

}
