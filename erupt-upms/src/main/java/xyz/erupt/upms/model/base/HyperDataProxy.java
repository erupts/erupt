package xyz.erupt.upms.model.base;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.service.EruptUserService;

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
        hyperModel.setCreateUser(new EruptUser(eruptUserService.getCurrentUid()));
    }

    @Override
    public void beforeUpdate(HyperModel hyperModel) {
        hyperModel.setUpdateTime(new Date());
        hyperModel.setUpdateUser(new EruptUser(eruptUserService.getCurrentUid()));
    }
}
