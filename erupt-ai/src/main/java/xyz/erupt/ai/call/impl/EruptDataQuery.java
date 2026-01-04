package xyz.erupt.ai.call.impl;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import xyz.erupt.ai.annotation.AiParam;
import xyz.erupt.ai.call.AiFunctionCall;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.jpa.dao.EruptDao;

import java.util.List;

/**
 * @author YuePeng
 * date 2025/3/14 23:25
 */
@Component
@Scope("prototype")
public class EruptDataQuery implements AiFunctionCall {

    @AiParam(description = "HQL (Hibernate Query Language)")
    private String hql;

    @Resource
    private EruptDao eruptDao;

    @Override
    public String description() {
        return "Query erupt model data";
    }

    @Override
    public String call(String prompt) {
        List<?> result = eruptDao.getEntityManager().createQuery(hql).getResultList();
        return GsonFactory.getGson().toJson(result);
    }
}
