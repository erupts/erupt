package xyz.erupt.ai.call;

import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.stereotype.Component;
import xyz.erupt.ai.annotation.AiFuncParam;
import xyz.erupt.ai.base.SuperLLM;
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

    @Getter
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
            Optional.ofNullable(field.getAnnotation(AiFuncParam.class)).ifPresent(it -> {
                params.put(field.getName(), field);
            });
        }
        if (params.isEmpty()) {
            return aiFunctionCall.call(userMessage);
        } else {
            Map<String, ParamPromptTemplate> promptTemplateMap = getStringParamPromptTemplateMap(params);
            StringBuilder prompt = new StringBuilder();
            prompt.append(userMessage).append("\n\n");
            prompt.append("根据上面的内容生成且填充下面json每个key的value字段，目前json的每个value是具体的生成要求\n");
            prompt.append("请严格按照以下JSON格式返回，不要返回其他任何多余的内容或解释，不要带markdown格式：\n\n");
            prompt.append(GsonFactory.getGson().toJson(promptTemplateMap));

            String llmRes = SuperLLM.getLLM(llm).chat(llm, prompt.toString(), userContext).getMessageStr();
            try {
                Map<String, ParamPromptTemplate> res = GsonFactory.getGson().fromJson(llmRes, new TypeToken<Map<String, ParamPromptTemplate>>() {
                }.getType());
                for (Map.Entry<String, ParamPromptTemplate> entry : res.entrySet()) {
                    Field field = aiFunctionCall.getClass().getDeclaredField(entry.getKey());
                    field.setAccessible(true);
                    field.set(aiFunctionCall, entry.getValue().getValue());
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
            AiFuncParam aiFuncParam = entry.getValue().getAnnotation(AiFuncParam.class);
            ParamPromptTemplate promptTemplate = new ParamPromptTemplate();
            promptTemplate.setDescription(aiFuncParam.description());
            promptTemplate.setRequired(aiFuncParam.required());
            promptTemplate.setType(entry.getValue().getType().getSimpleName());
            promptTemplateMap.put(entry.getKey(), promptTemplate);
        }
        return promptTemplateMap;
    }

}
