package xyz.erupt.ai.call;

import com.google.gson.reflect.TypeToken;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.stereotype.Component;
import xyz.erupt.ai.annotation.AiParam;
import xyz.erupt.ai.constants.ResponseFormat;
import xyz.erupt.ai.core.LlmCore;
import xyz.erupt.ai.core.LlmRequest;
import xyz.erupt.ai.model.LLM;
import xyz.erupt.ai.pojo.ChatCompletionMessage;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.service.EruptApplication;
import xyz.erupt.core.util.EruptSpringUtil;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author YuePeng
 * date 2025/3/14 23:42
 */
@Component
@Order(100)
@Slf4j
public class AiFunctionManager implements ApplicationRunner {

    private final Map<String, AiFunctionCall> aiFunctionMap = new HashMap<>();

    @Override
    public void run(ApplicationArguments args) {
        EruptSpringUtil.scannerPackage(EruptApplication.getScanPackage(),
                new TypeFilter[]{new AssignableTypeFilter(AiFunctionCall.class)}, clazz ->
                        aiFunctionMap.put(clazz.getSimpleName(), (AiFunctionCall) EruptSpringUtil.getBean(clazz))
        );
    }

    public String getFunctionCallPrompt() {
        StringBuilder sb = new StringBuilder("下面是一组 Function Call 的映射，根据情况决定是否调用，否则忽略这段提示词\n");
        for (Map.Entry<String, AiFunctionCall> entry : aiFunctionMap.entrySet()) {
            sb.append("- 如果用户问：").append(entry.getValue().description()).append("，就只回复：").append(entry.getKey()).append("\n");
        }
        return sb.toString();
    }

    public boolean exist(String key) {
        return aiFunctionMap.containsKey(key);
    }

    @SneakyThrows
    public String call(String key, LLM llm, String userMessage, List<ChatCompletionMessage> userContext) {
        AiFunctionCall aiFunctionCall = aiFunctionMap.get(key);
        Map<String, Field> params = new HashMap<>();
        for (Field field : aiFunctionCall.getClass().getDeclaredFields()) {
            Optional.ofNullable(field.getAnnotation(AiParam.class)).ifPresent(it -> {
                params.put(field.getName(), field);
            });
        }
        if (params.isEmpty()) {
            return aiFunctionCall.call(userMessage);
        } else {
            Map<String, ParamPromptTemplate> promptTemplateMap = getStringParamPromptTemplateMap(params);
            StringBuilder prompt = new StringBuilder();
            prompt.append(userMessage).append("\n\n");
            prompt.append("根据上面的内容，自动识别并填充下面JSON的val字段，此JSON中的每个value都是具体的生成要求，将不同key的识别结果放到对应val字段内\n");
            prompt.append("请严格按照以下JSON格式返回，不要返回其他任何多余的内容或解释，请确保只返回纯JSON，不要混用Markdown格式：\n\n");
            prompt.append(GsonFactory.getGson().toJson(promptTemplateMap));
            LlmRequest llmRequest = llm.toLlmRequest();
            llmRequest.setResponseFormat(ResponseFormat.json_object);
            String llmRes = LlmCore.getLLM(llm).chat(llm.toLlmRequest(), prompt.toString(), userContext).getMessageStr();
            try {
                Map<String, ParamPromptTemplate> res = GsonFactory.getGson().fromJson(llmRes, new TypeToken<Map<String, ParamPromptTemplate>>() {
                }.getType());
                for (Map.Entry<String, ParamPromptTemplate> entry : res.entrySet()) {
                    Field field = aiFunctionCall.getClass().getDeclaredField(entry.getKey());
                    field.setAccessible(true);
                    field.set(aiFunctionCall, entry.getValue().getVal());
                    field.setAccessible(false);
                }
            } catch (Exception e) {
                throw new EruptWebApiRuntimeException("Function Call param error: " + e.getMessage() + "→ \n\n" + llmRes);
            }
            return aiFunctionCall.call(prompt.toString());
        }
    }

    private static Map<String, ParamPromptTemplate> getStringParamPromptTemplateMap(Map<String, Field> params) {
        Map<String, ParamPromptTemplate> promptTemplateMap = new HashMap<>();
        for (Map.Entry<String, Field> entry : params.entrySet()) {
            AiParam aiFuncParam = entry.getValue().getAnnotation(AiParam.class);
            ParamPromptTemplate promptTemplate = new ParamPromptTemplate();
            promptTemplate.setDescription(aiFuncParam.description());
            promptTemplate.setRequired(aiFuncParam.required());
            promptTemplate.setType(entry.getValue().getType().getSimpleName());
            promptTemplateMap.put(entry.getKey(), promptTemplate);
        }
        return promptTemplateMap;
    }

}
