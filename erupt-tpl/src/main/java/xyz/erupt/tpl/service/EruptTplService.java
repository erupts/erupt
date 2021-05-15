package xyz.erupt.tpl.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.LinkedCaseInsensitiveMap;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.annotation.sub_erupt.Tpl;
import xyz.erupt.core.service.EruptApplication;
import xyz.erupt.core.toolkit.TimeRecorder;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.tpl.annotation.EruptTpl;
import xyz.erupt.tpl.annotation.TplAction;
import xyz.erupt.tpl.engine.*;
import xyz.erupt.upms.enums.MenuTypeEnum;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Writer;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author YuePeng
 * date 2020-02-24
 */
@Order
@Service
@Slf4j
public class EruptTplService implements ApplicationRunner {

    public static String TPL = "tpl";

    private static final Map<Tpl.Engine, EngineTemplate<Object>> tplEngines = new HashMap<>();

    private static final Class<?>[] engineTemplates = {
            NativeEngine.class,
            FreemarkerEngine.class,
            ThymeleafEngine.class,
            VelocityTplEngine.class,
            BeetlEngine.class
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

    @Resource
    private HttpServletRequest request;

    @Resource
    private HttpServletResponse response;

    @Override
    public void run(ApplicationArguments args) {
        TimeRecorder timeRecorder = new TimeRecorder();
        EruptSpringUtil.scannerPackage(EruptApplication.getScanPackage(), new TypeFilter[]{
                new AnnotationTypeFilter(EruptTpl.class)
        }, clazz -> {
            for (Method method : clazz.getDeclaredMethods()) {
                TplAction tplAction = method.getAnnotation(TplAction.class);
                if (null != tplAction) {
                    tplActions.put(tplAction.value(), method);
                }
            }
        });
        MenuTypeEnum.addMenuType(new VLModel(TPL, "模板", "tpl目录下文件名"));
        log.info("Erupt tpl initialization completed in {} ms", timeRecorder.recorder());
    }

    public Method getAction(String name) {
        return tplActions.get(name);
    }

    @SneakyThrows
    public void tplRender(Tpl tpl, Map<String, Object> map, HttpServletResponse response) {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        this.tplRender(tpl, map, response.getWriter());
    }

    public void tplRender(Tpl tpl, Map<String, Object> map, Writer writer) {
        if (!tpl.tplHandler().isInterface()) {
            if (null == map) {
                map = new HashMap<>();
            }
            Tpl.TplHandler tplHandler = EruptSpringUtil.getBean(tpl.tplHandler());
            tplHandler.bindTplData(map, tpl.params());
        }
        this.tplRender(tpl.engine(), tpl.path(), map, writer);
    }

    @SneakyThrows
    public void tplRender(Tpl.Engine engine, String path, Map<String, Object> map, Writer writer) {
        if (null == map) {
            map = new HashMap<>();
        }
        map.put(EngineConst.INJECT_REQUEST, request);
        map.put(EngineConst.INJECT_RESPONSE, response);
        map.put(EngineConst.INJECT_BASE, request.getContextPath());
        EngineTemplate<Object> engineAbstractTemplate = tplEngines.get(engine);
        Assert.notNull(engineAbstractTemplate, engine.name() + " jar not found");
        engineAbstractTemplate.render(engineAbstractTemplate.getEngine(), path, map, writer);
    }

}
