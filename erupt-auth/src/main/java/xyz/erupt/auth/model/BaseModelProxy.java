package xyz.erupt.auth.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.auth.service.EruptUserService;

/**
 * @author liyuepeng
 * @date 2018-10-11.
 */
@Service
public class BaseModelProxy implements DataProxy<BaseModel> {

    @Autowired
    private EruptUserService userService;

    @Override
    public void beforeAdd(BaseModel baseModel) {
        EruptUser eruptUser = new EruptUser();
        eruptUser.setId(userService.getCurrentUid());
//        baseModel.setCreateTime(new Date());
//        baseModel.setCreateUser(eruptUser);
    }

    @Override
    public void beforeUpdate(BaseModel baseModel) {
        EruptUser eruptUser = new EruptUser();
        eruptUser.setId(userService.getCurrentUid());
//        baseModel.setUpdateTime(new Date());
//        baseModel.setUpdateUser(eruptUser);
    }
}
