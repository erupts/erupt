package com.erupt.model.proxy;

import com.erupt.annotation.fun.DataProxy;
import com.erupt.annotation.model.BoolAndReason;
import com.erupt.model.EruptUser;
import com.erupt.util.MD5Utils;

/**
 * @Description:
 * @author: liyuepeng
 * @time:2018/12/15 22:49
 */
public class EruptUserProxy implements DataProxy {
    @Override
    public BoolAndReason beforeSave(Object o) {
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
    public void afterSave(Object o) {

    }

    @Override
    public BoolAndReason beforeDelete(Object o) {
        return null;
    }

    @Override
    public void afterDelete(Object o) {

    }

    @Override
    public BoolAndReason beforeFetch(Object o) {
        return null;
    }

    @Override
    public void afterFetch(Object o) {

    }
}
