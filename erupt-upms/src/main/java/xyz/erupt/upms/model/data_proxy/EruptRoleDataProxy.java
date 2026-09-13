package xyz.erupt.upms.model.data_proxy;

import jakarta.annotation.Resource;
import jakarta.persistence.Transient;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.model.EruptRole;


/**
 * @author YuePeng
 * date 2022/9/1 22:30
 */
@Service
public class EruptRoleDataProxy implements DataProxy<EruptRole> {

    @Resource
    @Transient
    private EruptDao eruptDao;

    @Override
    public void beforeAdd(EruptRole eruptRole) {
        Integer max = (Integer) eruptDao.lambdaQuery(EruptRole.class).max(EruptRole::getSort);
        eruptRole.setSort(null == max ? 10 : max + 10);
    }

}
