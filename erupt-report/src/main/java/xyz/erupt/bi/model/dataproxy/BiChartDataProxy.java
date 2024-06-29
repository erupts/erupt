package xyz.erupt.bi.model.dataproxy;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.bi.model.BiChart;
import xyz.erupt.bi.model.BiHistory;
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
            Integer sort = (Integer) eruptDao.lambdaQuery(BiChart.class).addCondition("bi.id = " + biChart.getBi().getId()).max(BiChart::getSort);
            biChart.setSort((sort == null) ? 10 : sort + 10);
        }
    }


    @Override
    public void beforeUpdate(BiChart biChart) {
        eruptDao.detach(biChart);
        BiChart hbc = eruptDao.lambdaQuery(BiChart.class).eq(BiChart::getId, biChart.getId()).one();
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
