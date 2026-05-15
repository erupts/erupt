package xyz.erupt.ai.core;

import com.google.gson.JsonObject;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fusesource.jansi.Ansi;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.ai.AiToolbox;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.core.config.GsonFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.fusesource.jansi.Ansi.ansi;

@Order(100)
@Service
@Slf4j
public class AiToolboxManager implements ApplicationRunner, ChoiceFetchHandler {

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
        if (aiMethodMap.isEmpty()) {
            log.info("AiToolbox initialized, 0 tool(s) registered");
        } else {
            Map<String, List<String>> classMethodMap = new java.util.LinkedHashMap<>();
            aiMethodBeanMap.forEach((name, bean) ->
                    classMethodMap.computeIfAbsent(bean.getClass().getSimpleName(), k -> new ArrayList<>()).add(name));
            StringBuilder sb = new StringBuilder("AiToolbox initialized, ")
                    .append(ansi().fgBright(Ansi.Color.CYAN).a(aiMethodMap.size()).reset())
                    .append(" tool(s) registered:");
            List<String> classes = new ArrayList<>(classMethodMap.keySet());
            for (int i = 0; i < classes.size(); i++) {
                String className = classes.get(i);
                String methods = String.join(", ", classMethodMap.get(className));
                sb.append("\n  ")
                        .append(ansi().fgBright(Ansi.Color.BLACK).a(i == classes.size() - 1 ? "└─ " : "├─ ").reset())
                        .append(ansi().fg(Ansi.Color.CYAN).a(className).reset())
                        .append(ansi().fgBright(Ansi.Color.BLACK).a(" [").reset())
                        .append(ansi().fgBright(Ansi.Color.BLACK).a(methods).reset())
                        .append(ansi().fgBright(Ansi.Color.BLACK).a("]").reset());
            }
            log.info(sb.toString());
        }
    }

    @Override
    public List<VLModel> fetch(String[] params) {
        return AiToolboxManager.getAiMethodMap().keySet().stream()
                .map(name -> new VLModel(name, name))
                .toList();
    }

}
