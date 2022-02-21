package xyz.erupt.bi.model;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.jpa.dao.EruptDao;

import javax.annotation.Resource;
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

    @Resource
    private EruptDao eruptDao;

    @Override
    public void beforeAdd(Bi bi) {
        bi.setCode(Erupts.generateCode());
    }

    @Override
    @Transactional
    public void beforeUpdate(Bi bi) {
        // TODO 在一对多的映射情况下，多的一方如果存有一的一方对象，那么这个对象必须赋值否则会出现多的一方数据无法保存的问题
        if (null != bi.getBiDimension()) {
            for (BiDimension dimension : bi.getBiDimension()) {
                dimension.setBi(bi);
            }
        }
    }

    @Override
    public void afterUpdate(Bi bi) {
        Bi bbi = eruptDao.getEntityManager().find(Bi.class, bi.getId());
        if (StringUtils.isNotBlank(bi.getSqlStatement()) && StringUtils.isNotBlank(bbi.getSqlStatement())) {
            if (!bi.getSqlStatement().equals(bbi.getSqlStatement())) {
                BiHistory bh = new BiHistory();
                bh.setBi(bi);
                bh.setSqlStatement(bbi.getSqlStatement());
                bh.setOperateTime(new Date());
                bh.setMark("Table");
                bh.setOperateBy(MetaContext.getUser().getName());
                eruptDao.persistAndFlush(bh);
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
