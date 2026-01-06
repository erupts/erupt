package xyz.erupt.ai.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import xyz.erupt.ai.config.AiProp;
import xyz.erupt.ai.core.LlmCore;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.annotation.sub_erupt.Tpl;
import xyz.erupt.annotation.sub_field.sub_edit.OnChange;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.linq.lambda.LambdaSee;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author YuePeng
 * date 2025/3/1 18:19
 */
@Component
public class LLMDataProxy implements DataProxy<LLM>, Tpl.TplHandler, OnChange<LLM>, OperationHandler<LLM, Void> {

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
    @Transactional
    public String exec(List<LLM> data, Void o, String[] param) {
        for (LLM llm : eruptDao.lambdaQuery(LLM.class).eq(LLM::getDefaultLLM, true).list()) {
            llm.setDefaultLLM(false);
            eruptDao.merge(llm);
        }
        eruptDao.find(LLM.class, data.get(0).getId()).setDefaultLLM(true);
        return "";
    }

    @Override
    public Map<String, Object> populateForm(LLM llm, String[] params) {
        if (null != llm.getLlm()) {
            Map<String, Object> ret = new HashMap<>();
            ret.put(LambdaSee.field(LLM::getModel), LlmCore.getLLM(llm.getLlm()).model());
            ret.put(LambdaSee.field(LLM::getApiUrl), LlmCore.getLLM(llm.getLlm()).api());
            ret.put(LambdaSee.field(LLM::getApiKey), "");
            ret.put(LambdaSee.field(LLM::getConfig), gson.toJson(LlmCore.getLLM(llm.getLlm()).config()));
            return ret;
        }
        return Map.of();
    }

    @Override
    public Map<String, String> buildEditExpr(LLM llm, String[] params) {
        return Map.of();
    }
}
