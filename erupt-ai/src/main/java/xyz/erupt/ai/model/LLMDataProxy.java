package xyz.erupt.ai.model;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xyz.erupt.ai.config.AiProp;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.sub_erupt.Tpl;
import xyz.erupt.jpa.dao.EruptDao;

import java.util.Map;

/**
 * @author YuePeng
 * date 2025/3/1 18:19
 */
@Component
public class LLMDataProxy implements DataProxy<LLM>, Tpl.TplHandler {

    @Resource
    private EruptDao eruptDao;

    @Resource
    private AiProp aiProp;

    @Override
    public void beforeAdd(LLM llm) {
        llm.setDefaultLLM(eruptDao.lambdaQuery(LLM.class).count() == 0);
    }

    @Override
    public void bindTplData(Map<String, Object> binding, String[] params) {
        binding.put("x", aiProp);
    }

}
