package xyz.erupt.report.model.dataproxy;

import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.linq.lambda.LambdaSee;
import xyz.erupt.report.model.Bi;
import xyz.erupt.report.model.BiHistory;

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
    public void addBehavior(Bi bi) {
        bi.setCode(Erupts.generateCode());
    }

    @Override
    @Transactional
    public void beforeUpdate(Bi bi) {
        eruptDao.detach(bi);
        Bi bbi = eruptDao.lambdaQuery(Bi.class).eq(Bi::getId, bi.getId()).one();
        // in a one-to-many mapping, the many-side must have the one-side set, otherwise the many-side data cannot be saved
//        if (null != bi.getBiDimension()) {
//            for (BiDimension dimension : bi.getBiDimension()) {
//                dimension.setBi(bi);
//            }
//        }
        if (StringUtils.isNotBlank(bi.getSqlStatement()) && StringUtils.isNotBlank(bbi.getSqlStatement())) {
            if (!bi.getSqlStatement().equals(bbi.getSqlStatement())) {
                BiHistory history = new BiHistory();
                history.setSqlStatement(bbi.getSqlStatement());
                history.setAfterSqlStatement(bi.getSqlStatement());
                history.setOperateTime(new Date());
                history.setMark(bi.getName());
                history.setBiId(bi.getId());
                history.setOperateBy(MetaContext.getUser().getName());
                eruptDao.persist(history);
            }
        }
    }

    @Override
    public void afterFetch(Collection<Map<String, Object>> list) {
        for (Map<String, Object> map : list) {
            map.put(LambdaSee.field(Bi::getView), "#/fill/bi/" + map.get("code"));
        }
    }
}
