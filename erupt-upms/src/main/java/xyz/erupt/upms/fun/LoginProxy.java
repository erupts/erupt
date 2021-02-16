package xyz.erupt.upms.fun;

import xyz.erupt.annotation.config.Comment;
import xyz.erupt.upms.model.EruptMenu;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.vo.BoolReasonValue;

import java.util.Collection;

/**
 * @author liyuepeng
 * @date 2021/2/13 20:11
 */
public interface LoginProxy<T> {

    @Comment("登录校验")
    default BoolReasonValue<T> login(String account, String pwd) {
        return new BoolReasonValue<T>(true, null);
    }

    @Comment("登录成功事件")
    void loginSuccess(EruptUser eruptUser, String token, Collection<EruptMenu> eruptMenus, T value);

}
