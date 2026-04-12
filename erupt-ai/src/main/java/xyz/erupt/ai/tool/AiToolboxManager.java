package xyz.erupt.ai.tool;

import com.google.gson.JsonObject;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.ai.AiToolbox;
import xyz.erupt.core.config.GsonFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Order(100)
@Service
@Slf4j
public class AiToolboxManager implements ApplicationRunner {

    @Resource
    private ApplicationContext applicationContext;

    @Getter
    private static final Map<String, Method> aiMethodMap = new HashMap<>();

    private static final Map<String, Object> aiMethodBeanMap = new HashMap<>();

    @Getter
    private static final List<Object> tools = new ArrayList<>();


    @SneakyThrows
    public static Object invoke(ToolExecutionRequest request) {
        Method method = aiMethodMap.get(request.name());
        if (null != method) {
            Object[] args = new Object[method.getParameterCount()];
            if (StringUtils.isNotBlank(request.arguments())) {
                JsonObject jsonObject = GsonFactory.getGson().fromJson(request.arguments(), JsonObject.class);
                for (int i = 0; i < method.getParameters().length; i++) {
                    String paramName = method.getParameters()[i].getName();
                    if (jsonObject.has(paramName)) {
                        args[i] = GsonFactory.getGson().fromJson(jsonObject.get(paramName), method.getGenericParameterTypes()[i]);
                    }
                }
            }
            return method.invoke(aiMethodBeanMap.get(request.name()), args);
        }
        return null;
    }

    @Override
    public void run(ApplicationArguments args) {
        applicationContext.getBeansWithAnnotation(AiToolbox.class).values().forEach(bean -> {
            Object target = AopProxyUtils.getSingletonTarget(bean);
            Object realBean = target != null ? target : bean;
            tools.add(realBean);
            for (Method method : realBean.getClass().getDeclaredMethods()) {
                if (method.isAnnotationPresent(Tool.class)) {
                    aiMethodMap.put(method.getName(), method);
                    aiMethodBeanMap.put(method.getName(), realBean);
                }
            }
        });
        log.info("AiToolbox initialized, {} tool(s) registered", aiMethodMap.size());
    }
}
