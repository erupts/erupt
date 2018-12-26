package xyz.erupt.eruptlimit.model.proxy;

import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.model.BoolAndReason;
import xyz.erupt.eruptlimit.constant.LimitConst;
import xyz.erupt.eruptlimit.model.EruptUser;
import xyz.erupt.util.MD5Utils;

/**
 * @Description:
 * @author: liyuepeng
 * @time:2018/12/15 22:49
 */
public class EruptUserProxy extends DataProxy {

    @Override
    public BoolAndReason beforeAdd(Object o) {
        EruptUser eruptUser = (EruptUser) o;
        if (eruptUser.getPassword().equals(eruptUser.getPassword2())) {
            if (eruptUser.getIsMD5()) {
                eruptUser.setPassword(MD5Utils.digestSalt(eruptUser.getPassword(), LimitConst.ERUPT_MD5_SALT));
            }
            return new BoolAndReason(true, null);
        } else {
            return new BoolAndReason(false, "两次密码输入不一致");
        }
    }
}
