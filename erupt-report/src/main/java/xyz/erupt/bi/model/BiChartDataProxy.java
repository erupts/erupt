package xyz.erupt.bi.model;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.jpa.dao.EruptDao;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author YuePeng
 * date 2023/6/4 17:55
 */
@Component
public class BiChartDataProxy implements DataProxy<BiChart> {

    @Resource
    private EruptDao eruptDao;

    @Override
    public void addBehavior(BiChart biChart) {
        biChart.setCode(RandomStringUtils.randomAlphabetic(8));
    }

    @Override
    public void beforeAdd(BiChart biChart) {
        if (null == biChart.getSort()) {
            Integer obj = (Integer) eruptDao.getEntityManager().createQuery(
                    "select max(sort) from " + BiChart.class.getSimpleName() + " where bi.id = " + biChart.getBi().getId()).getSingleResult();
            biChart.setSort((obj == null) ? 10 : obj + 10);
        }
    }


    @Override
    public void beforeUpdate(BiChart biChart) {
        eruptDao.getEntityManager().clear();
        BiChart hbc = eruptDao.getEntityManager().find(BiChart.class, biChart.getId());
        if (!biChart.getSqlStatement().equals(hbc.getSqlStatement())) {
            BiHistory history = new BiHistory();
            history.setBiId(biChart.getBi().getId());
            history.setSqlStatement(hbc.getSqlStatement());
            history.setAfterSqlStatement(biChart.getSqlStatement());
            history.setOperateTime(new Date());
            history.setMark(biChart.getName());
            history.setOperateBy(MetaContext.getUser().getName());
            eruptDao.persist(history);
        }
    }

}
