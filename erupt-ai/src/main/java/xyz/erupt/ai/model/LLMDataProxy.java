package xyz.erupt.ai.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import xyz.erupt.ai.config.AiProp;
import xyz.erupt.ai.core.LlmCore;
import xyz.erupt.annotation.fun.ChoiceTrigger;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.annotation.sub_erupt.Tpl;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.linq.lambda.LambdaSee;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author YuePeng
 * date 2025/3/1 18:19
 */
@Component
public class LLMDataProxy implements DataProxy<LLM>, Tpl.TplHandler, ChoiceTrigger, OperationHandler<LLM, Void> {

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

    public static Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();

    @Override
    public Map<String, Object> trigger(Object code, String[] params) {
        if (null != code && !"null".equals(code)) {
            Map<String, Object> ret = new HashMap<>();
            ret.put(LambdaSee.field(LLM::getModel), LlmCore.getLLM(code.toString()).model());
            ret.put(LambdaSee.field(LLM::getApiUrl), LlmCore.getLLM(code.toString()).api());
            ret.put(LambdaSee.field(LLM::getApiKey), "");
            ret.put(LambdaSee.field(LLM::getConfig), gson.toJson(LlmCore.getLLM(code.toString()).config()));
            return ret;
        }
        return Collections.emptyMap();
    }

    @Override
    @Transactional
    public String exec(List<LLM> data, Void o, String[] param) {
        for (LLM llm : eruptDao.lambdaQuery(LLM.class).eq(LLM::getDefaultLLM, true).list()) {
            llm.setDefaultLLM(false);
            eruptDao.merge(llm);
        }
        eruptDao.find(LLM.class, data.get(0).getId()).setDefaultLLM(true);
        return "";
    }
}
