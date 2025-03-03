package xyz.erupt.ai.model;

import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.jpa.dao.EruptDao;

import javax.annotation.Resource;

/**
 * @author YuePeng
 * date 2025/3/1 18:19
 */
@Component
public class LLMDataProxy implements DataProxy<LLM> {

    @Resource
    private EruptDao eruptDao;

    @Override
    public void beforeAdd(LLM llm) {
        llm.setDefaultLLM(eruptDao.lambdaQuery(LLM.class).count() == 0);
    }
}
