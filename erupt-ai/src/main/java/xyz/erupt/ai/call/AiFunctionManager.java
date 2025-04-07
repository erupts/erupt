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
import xyz.erupt.ai.pojo.ChatCompletionMessage;
import xyz.erupt.core.config.GsonFactory;
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

    public String getFunctionCallParamsPrompt() {
        return null;
    }

    public boolean exist(String key) {
        return aiFunctionMap.containsKey(key);
    }

    @SneakyThrows
    public String call(String key, SuperLLM llm, String userMessage, List<ChatCompletionMessage> userContext) {
        AiFunctionCall aiFunctionCall = aiFunctionMap.get(key);
        Map<String, String> params = new HashMap<>();
        for (Field field : aiFunctionCall.getClass().getDeclaredFields()) {
            Optional.ofNullable(field.getAnnotation(AiFuncParam.class)).ifPresent(it -> {
                params.put(field.getName(), it.value() + ",数据类型：" + field.getType().getSimpleName());
            });
        }
        if (params.isEmpty()) {
            return aiFunctionCall.call(userMessage);
        } else {
            StringBuilder prompt = new StringBuilder();
            prompt.append("根据以下内容，提取出JSON：\n");
            prompt.append(userMessage);
            prompt.append("请严格按照以下JSON格式返回内容，不要返回其他任何多余的内容或解释：\n");
            prompt.append(GsonFactory.getGson().toJson(params));
            String llmRes = llm.chat(null, prompt.toString(), userContext).getMessageStr();
            Map<String, Object> res = GsonFactory.getGson().fromJson(llmRes, new TypeToken<Map<String, Object>>() {
            }.getType());
            for (Map.Entry<String, Object> entry : res.entrySet()) {
                Field field = aiFunctionCall.getClass().getField(entry.getKey());
                field.setAccessible(true);
                field.set(aiFunctionCall, entry.getValue());
                field.setAccessible(false);
            }
            return aiFunctionCall.call(prompt.toString());
        }
    }

}
