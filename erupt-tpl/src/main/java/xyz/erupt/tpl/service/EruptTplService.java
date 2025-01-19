package xyz.erupt.tpl.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.util.LinkedCaseInsensitiveMap;
import org.springframework.util.PathMatcher;
import xyz.erupt.annotation.sub_erupt.Tpl;
import xyz.erupt.core.service.EruptApplication;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.tpl.annotation.EruptTpl;
import xyz.erupt.tpl.annotation.TplAction;
import xyz.erupt.tpl.engine.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Writer;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author YuePeng
 * date 2020-02-24
 */
@Order
@Service
@Slf4j
public class EruptTplService {

    public static String TPL = "tpl";

    public static String TPL_MICRO = "mtpl";

    private static final Map<Tpl.Engine, EngineTemplate<Object>> tplEngines = new HashMap<>();

    private static final Class<?>[] engineTemplates = {
            NativeEngine.class,
            FreemarkerEngine.class,
            ThymeleafEngine.class,
            VelocityTplEngine.class,
            BeetlEngine.class,
            EnjoyEngine.class
    };

    static {
        for (Class<?> tpl : engineTemplates) {
            try {
                EngineTemplate<Object> engineTemplate = (EngineTemplate) tpl.newInstance();
                engineTemplate.setEngine(engineTemplate.init());
                tplEngines.put(engineTemplate.engine(), engineTemplate);
            } catch (NoClassDefFoundError ignored) {
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private final Map<String, Method> tplActions = new LinkedCaseInsensitiveMap<>();

    private final Map<String, Method> tplMatcherActions = new LinkedCaseInsensitiveMap<>();

    private final PathMatcher pathMatcher = new AntPathMatcher();

    @Resource
    private HttpServletRequest request;

    @Resource
    private HttpServletResponse response;

    public void run() {
        EruptSpringUtil.scannerPackage(EruptApplication.getScanPackage(),
                new TypeFilter[]{new AnnotationTypeFilter(EruptTpl.class)},
                this::registerTpl);
    }

    //注册模板
    public void registerTpl(Class<?> tplClass) {
        Arrays.stream(tplClass.getDeclaredMethods()).forEach(method -> Optional.ofNullable(method.getAnnotation(TplAction.class))
                .ifPresent(it -> {
                            if (pathMatcher.isPattern(it.value())) {
                                tplMatcherActions.put(it.value(), method);
                            } else {
                                tplActions.put(it.value(), method);
                            }
                        }
                ));
    }

    //移除模板
    public void unregisterTpl(Class<?> tplClass) {
        Arrays.stream(tplClass.getDeclaredMethods()).forEach(
                method -> Optional.ofNullable(method.getAnnotation(TplAction.class)).ifPresent(
                        it -> {
                            tplActions.remove(it.value());
                            tplMatcherActions.remove(it.value());
                        }
                )
        );
    }

    public Method getAction(String path) {
        if (tplActions.containsKey(path)) {
            return tplActions.get(path);
        } else {
            // 从模糊匹配中查询资源路径
            for (Map.Entry<String, Method> entry : tplMatcherActions.entrySet()) {
                if (pathMatcher.match(entry.getKey(), path)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    public Object getEngine(Tpl.Engine engine) {
        return tplEngines.get(engine).getEngine();
    }

    @SneakyThrows
    public void tplRender(Tpl tpl, Map<String, Object> map, HttpServletResponse response) {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        this.tplRender(tpl, map, response.getWriter());
    }

    public void tplRender(Tpl tpl, Map<String, Object> map, Writer writer) {
        if (!tpl.tplHandler().isInterface()) {
            Tpl.TplHandler tplHandler = EruptSpringUtil.getBean(tpl.tplHandler());
            map = Optional.ofNullable(map).orElse(new HashMap<>());
            tplHandler.bindTplData(map, tpl.params());
        }
        this.tplRender(tpl.engine(), tpl.path(), map, writer);
    }

    @SneakyThrows
    public void tplRender(Tpl.Engine engine, String path, Map<String, Object> map, Writer writer) {
        map = Optional.ofNullable(map).orElse(new HashMap<>());
        map.put(EngineConst.INJECT_REQUEST, request);
        map.put(EngineConst.INJECT_RESPONSE, response);
        map.put(EngineConst.INJECT_BASE, request.getContextPath());
        EngineTemplate<Object> engineAbstractTemplate = tplEngines.get(engine);
        Assert.notNull(engineAbstractTemplate, engine.name() + " jar not found");
        if (path.contains("?")) {
            String[] sp = path.split("\\?");
            path = sp[0];
            for (String kv : sp[1].split("&")) {
                map.put(kv.split("=")[0], kv.split("=")[1]);
            }
        }
        engineAbstractTemplate.render(engineAbstractTemplate.getEngine(), path, map, writer);
    }

}
