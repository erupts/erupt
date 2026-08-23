package xyz.erupt.ai.handler;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xyz.erupt.ai.core.EmbeddingCore;
import xyz.erupt.ai.model.EmbeddingLLM;
import xyz.erupt.ai.service.EmbeddingModelService;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.sub_field.sub_edit.OnChange;
import xyz.erupt.linq.lambda.LambdaSee;

import java.util.HashMap;
import java.util.Map;

/**
 * @author YuePeng
 * date 2026/8/17
 */
@Component
public class EmbeddingLLMDataProxy implements DataProxy<EmbeddingLLM>, OnChange<EmbeddingLLM> {

    @Resource
    private EmbeddingModelService embeddingModelService;

    @Override
    public void afterUpdate(EmbeddingLLM embeddingLLM) {
        embeddingModelService.evict(embeddingLLM.getId());
    }

    @Override
    public void afterDelete(EmbeddingLLM embeddingLLM) {
        embeddingModelService.evict(embeddingLLM.getId());
    }

    @Override
    public Map<String, Object> populateForm(EmbeddingLLM embeddingLLM, String[] params) {
        EmbeddingCore core = EmbeddingCore.get(embeddingLLM.getProvider());
        if (null == core) {
            return Map.of();
        }
        Map<String, Object> ret = new HashMap<>();
        ret.put(LambdaSee.field(EmbeddingLLM::getModel), core.model());
        ret.put(LambdaSee.field(EmbeddingLLM::getApiUrl), core.api());
        ret.put(LambdaSee.field(EmbeddingLLM::getDimension), core.dimension());
        return ret;
    }

    @Override
    public Map<String, String> buildEditExpr(EmbeddingLLM embeddingLLM, String[] params) {
        return Map.of();
    }

}
