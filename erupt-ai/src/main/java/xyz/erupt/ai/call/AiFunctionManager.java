package xyz.erupt.ai.call;

import lombok.Getter;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.stereotype.Component;
import xyz.erupt.core.service.EruptApplication;
import xyz.erupt.core.util.EruptSpringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author YuePeng
 * date 2025/3/14 23:42
 */
@Component
@Order(100)
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
            sb.append("- 如果用户问：").append(entry.getValue().description()).append("，就回复：").append(entry.getKey()).append("\n");
        }
        return sb.toString();
    }

    public boolean exist(String key) {
        return aiFunctionMap.containsKey(key);
    }

    public String call(String key) {
        return aiFunctionMap.get(key).call("");
    }

}
