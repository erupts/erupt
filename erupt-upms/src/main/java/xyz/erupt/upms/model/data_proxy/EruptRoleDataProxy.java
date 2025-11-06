package xyz.erupt.upms.model.data_proxy;

import org.springframework.stereotype.Service;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.model.EruptRole;
import xyz.erupt.upms.service.EruptUserService;

import jakarta.annotation.Resource;
import jakarta.persistence.Transient;
import java.util.List;

/**
 * @author YuePeng
 * date 2022/9/1 22:30
 */
@Service
public class EruptRoleDataProxy implements DataProxy<EruptRole> {

    @Resource
    @Transient
    private EruptUserService eruptUserService;

    @Resource
    @Transient
    private EruptDao eruptDao;

    @Override
    public String beforeFetch(List<Condition> conditions) {
        if (eruptUserService.getCurrentEruptUser().getIsAdmin()) return null;
        return "EruptRole.createUser.id = " + eruptUserService.getCurrentUid();
    }

    @Override
    public void addBehavior(EruptRole eruptRole) {
        Integer max = (Integer) eruptDao.lambdaQuery(EruptRole.class).max(EruptRole::getSort);
        if (null == max) {
            eruptRole.setSort(10);
        } else {
            eruptRole.setSort(max + 10);
        }
    }

}
