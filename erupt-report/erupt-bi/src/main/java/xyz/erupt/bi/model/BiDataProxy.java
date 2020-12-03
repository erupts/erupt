package xyz.erupt.bi.model;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.auth.model.EruptUser;
import xyz.erupt.auth.service.EruptUserService;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * @author liyuepeng
 * @date 2019-08-26.
 */
@Component
public class BiDataProxy implements DataProxy<Bi> {

    @Autowired
    private EruptUserService eruptUserService;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void beforeUpdate(Bi bi) {
        entityManager.clear();
        Bi bbi = entityManager.find(Bi.class, bi.getId());
        if (StringUtils.isNotBlank(bi.getSqlStatement()) && StringUtils.isNotBlank(bbi.getSqlStatement())) {
            if (!bi.getSqlStatement().equals(bbi.getSqlStatement())) {
                BiHistory bh = new BiHistory();
                bh.setBi(bi);
                bh.setSqlStatement(bbi.getSqlStatement());
                bh.setOperateTime(new Date());
                bh.setOperateUser(new EruptUser(eruptUserService.getCurrentUid()));
                entityManager.persist(bh);
                entityManager.flush();
            }
        }
    }

    @Override
    public void afterFetch(Collection<Map<String, Object>> list) {
        for (Map<String, Object> map : list) {
            map.put("view", "#/fill/bi/" + map.get("code"));
        }
    }
}
