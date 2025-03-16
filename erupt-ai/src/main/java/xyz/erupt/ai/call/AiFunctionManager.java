package xyz.erupt.ai.call;

import lombok.Getter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.stereotype.Component;
import xyz.erupt.core.service.EruptApplication;
import xyz.erupt.core.util.EruptSpringUtil;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * @author YuePeng
 * date 2025/3/14 23:42
 */
@Component
public class AiFunctionManager {

    @Getter
    private final Map<String, AiFunction> aiFunctionMap = new HashMap<>();

    @PostConstruct
    public void init() {
        EruptSpringUtil.scannerPackage(EruptApplication.getScanPackage(),
                new TypeFilter[]{new AssignableTypeFilter(AiFunction.class)}, clazz ->
                        aiFunctionMap.put(clazz.getSimpleName(), (AiFunction) EruptSpringUtil.getBean(clazz))
        );
    }

    public String getFunctionCallPrompt() {
        StringBuilder sb = new StringBuilder("下面内容的格式是多组 key → desc，如果用户问题与desc相关，则仅仅只返回对应的key即可，其他的不用输出，否则忽略这段提示词\n");
        for (Map.Entry<String, AiFunction> entry : aiFunctionMap.entrySet()) {
            sb.append("- ").append(entry.getKey()).append(" → ").append(entry.getValue().name()).append("\n");
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
