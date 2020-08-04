package xyz.erupt.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.auth.model.EruptUser;
import xyz.erupt.auth.service.EruptUserService;

import java.util.Date;

/**
 * @author liyuepeng
 * @date 2020-08-04
 */
@Service
public class HyperDataProxy implements DataProxy<HyperModel> {

    @Autowired
    private EruptUserService eruptUserService;

    @Override
    public void beforeAdd(HyperModel hyperModel) {
        hyperModel.setCreateTime(new Date());
        EruptUser eruptUser = new EruptUser();
        eruptUser.setId(eruptUserService.getCurrentUid());
        hyperModel.setCreateUser(eruptUser);
    }

    @Override
    public void beforeUpdate(HyperModel hyperModel) {
        hyperModel.setUpdateTime(new Date());
        EruptUser eruptUser = new EruptUser();
        eruptUser.setId(eruptUserService.getCurrentUid());
        hyperModel.setUpdateUser(eruptUser);
    }
}
