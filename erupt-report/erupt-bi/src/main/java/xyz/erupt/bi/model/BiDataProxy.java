package xyz.erupt.bi.model;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.core.context.MetaContext;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * @author YuePeng
 * date 2019-08-26.
 */
@Component
public class BiDataProxy implements DataProxy<Bi> {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void addBehavior(Bi bi) {
        bi.setCode(RandomStringUtils.randomAlphabetic(6));
    }

    @Override
    @Transactional
    public void beforeUpdate(Bi bi) {
        //TODO clear 方法貌似存在一些问题
        entityManager.clear();
        Bi bbi = entityManager.find(Bi.class, bi.getId());
        // TODO 在一对多的映射情况下，多的一方如果存有一的一方对象，那么这个对象必须赋值否则会出现多的一方数据无法保存的问题
        if (null != bi.getBiDimension()) {
            for (BiDimension dimension : bi.getBiDimension()) {
                dimension.setBi(bi);
            }
        }
        if (StringUtils.isNotBlank(bi.getSqlStatement()) && StringUtils.isNotBlank(bbi.getSqlStatement())) {
            if (!bi.getSqlStatement().equals(bbi.getSqlStatement())) {
                BiHistory bh = new BiHistory();
                bh.setBi(bi);
                bh.setSqlStatement(bbi.getSqlStatement());
                bh.setOperateTime(new Date());
                bh.setMark("Table");
                bh.setOperateBy(MetaContext.get().getMetaUser().getName());
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
