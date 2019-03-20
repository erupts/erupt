package xyz.erupt.eruptlimit.model.proxy;

import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.model.BoolAndReason;
import xyz.erupt.core.model.Page;
import xyz.erupt.core.util.MD5Utils;
import xyz.erupt.eruptlimit.model.EruptUser;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @author: liyuepeng
 * @time:2018/12/15 22:49
 */
@Component
public class EruptUserProxy extends DataProxy<EruptUser> {


    @Override
    public BoolAndReason beforeAdd(EruptUser eruptUser) {
        if (eruptUser.getPassword().equals(eruptUser.getPassword2())) {
            if (eruptUser.getIsMD5()) {
                eruptUser.setPassword(MD5Utils.digest(eruptUser.getPassword()));
            }
            return new BoolAndReason(true, null);
        } else {
            return new BoolAndReason(false, "两次密码输入不一致");
        }
    }

    @Override
    public void afterFetch(Object o) {
        Page page = (Page) o;
        Collection<Map> lm = page.getList();
        Map<String, String> map = new HashMap();
        map.put("account", "2333");
        lm.add(map);
        super.afterFetch(o);
    }
}
