package xyz.erupt.upms.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.expr.ExprBool;
import xyz.erupt.upms.model.EruptMenu;
import xyz.erupt.upms.service.EruptUserService;

/**
 * @author liyuepeng
 * @date 2020/12/28 22:33
 */
@Service
@Comment("通过菜单编码控制是否显示")
public class ViaMenuCtrl implements ExprBool.ExprHandler {

    @Autowired
    private EruptUserService eruptUserService;

    @Override
    @Comment("params必填，值为菜单编码")
    public boolean boolHandler(boolean expr, String[] params) {
        if (null == params || params.length == 0) {
            throw new RuntimeException("ViaMenuCtrl → params[0] not found");
        }
        for (EruptMenu menu : eruptUserService.getCurrentEruptUserMenu()) {
            if (menu.getCode().equals(params[0])) {
                return true;
            }
        }
        return false;
    }

}
