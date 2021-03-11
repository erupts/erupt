package xyz.erupt.upms.model.base;

import org.springframework.stereotype.Service;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.service.EruptUserService;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author liyuepeng
 * @date 2020-08-04
 */
@Service
public class HyperDataProxyVo implements DataProxy<HyperModelVo> {

    @Resource
    private EruptUserService eruptUserService;

    @Override
    public void beforeAdd(HyperModelVo hyperModel) {
        hyperModel.setCreateTime(new Date());
        hyperModel.setCreateUser(new EruptUser(eruptUserService.getCurrentUid()));
    }

    @Override
    public void beforeUpdate(HyperModelVo hyperModel) {
        hyperModel.setUpdateTime(new Date());
        hyperModel.setUpdateUser(new EruptUser(eruptUserService.getCurrentUid()));
    }
}
