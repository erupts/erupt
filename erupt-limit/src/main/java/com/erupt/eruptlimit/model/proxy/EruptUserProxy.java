package com.erupt.eruptlimit.model.proxy;

import com.erupt.annotation.fun.DataProxy;
import com.erupt.annotation.model.BoolAndReason;
import com.erupt.eruptlimit.model.EruptUser;
import com.erupt.util.MD5Utils;

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
                eruptUser.setPassword(MD5Utils.digestSalt(eruptUser.getPassword()));
            }
            return new BoolAndReason(true, null);
        } else {
            return new BoolAndReason(false, "两次密码输入不一致");
        }
    }

    @Override
    public BoolAndReason beforeEdit(Object o) {
        return this.beforeAdd(o);
    }
}
