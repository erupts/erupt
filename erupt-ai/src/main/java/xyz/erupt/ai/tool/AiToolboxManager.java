package xyz.erupt.ai.tool;

import com.google.gson.JsonObject;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.stereotype.Service;
import xyz.erupt.ai.annotation.AiToolbox;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.service.EruptApplication;
import xyz.erupt.core.util.EruptSpringUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Order(100)
@Service
@Slf4j
public class AiToolboxManager implements ApplicationRunner {

    @Getter
    private static final Map<String, Method> aiMethodMap = new HashMap<>();

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
            return method.invoke(EruptSpringUtil.getBean(method.getDeclaringClass()), args);
        }
        return null;
    }

    @Override
    public void run(ApplicationArguments args) {
        EruptSpringUtil.scannerPackage(EruptApplication.getScanPackage(), new TypeFilter[]{
                new AnnotationTypeFilter(AiToolbox.class)
        }, clazz -> {
            if (clazz.isInterface()) {
                return;
            }
            Object bean = EruptSpringUtil.getBean(clazz);
            Object target = AopProxyUtils.getSingletonTarget(bean);
            tools.add(target != null ? target : bean);
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Tool.class)) {
                    aiMethodMap.put(method.getName(), method);
                }
            }
        });
    }
}
