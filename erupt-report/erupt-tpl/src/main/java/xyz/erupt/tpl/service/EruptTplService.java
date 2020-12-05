package xyz.erupt.tpl.service;

import freemarker.template.Configuration;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedCaseInsensitiveMap;
import org.springframework.util.StreamUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.annotation.sub_erupt.Tpl;
import xyz.erupt.core.service.EruptApplication;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.tpl.annotation.EruptTpl;
import xyz.erupt.tpl.annotation.TplAction;
import xyz.erupt.upms.util.MenuTool;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author liyuepeng
 * @date 2020-02-24
 */
@Order
@Service
@Slf4j
public class EruptTplService implements ApplicationRunner {

    private final Map<String, Method> tplActions = new LinkedCaseInsensitiveMap<>();

    @Autowired
    private TemplateEngine thymeleafEngine;

    @Autowired
    private Configuration freeMarkerEngine;

    @Resource
    private HttpServletRequest request;

    public Method getAction(String name) {
        return tplActions.get(name);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
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
        MenuTool.addMenuType(new VLModel("tpl", "模板", "tpl目录下文件名"));
        log.info("Erupt tpl initialization complete");
    }

    @SneakyThrows
    public void tplToResponse(Tpl tpl, HttpServletResponse response, Object rows) {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        Map<String, Object> data = new HashMap<>();
        if (!tpl.tplHandler().isInterface()) {
            data = EruptSpringUtil.getBean(tpl.tplHandler()).bindTplData(tpl.params());
        }
        data.put("rows", rows);
        response.getWriter().write(this.tplRender(tpl.path(), data, tpl.engine()));
    }

    @SneakyThrows
    public String tplRender(String path, Map<String, Object> data, Tpl.Engine engine) {
        if (data == null) {
            data = new HashMap<>();
        }
        data.put("request", request);
        switch (engine) {
            case FreeMarker:
                StringWriter out = new StringWriter();
                freeMarkerEngine.getTemplate(path).process(data, out);
                return out.toString();
            case Thymeleaf:
                Context ctx = new Context();
                ctx.setVariables(data);
                return thymeleafEngine.process(path, ctx);
            case Native:
                return StreamUtils.copyToString(this.getClass().getResourceAsStream(path), StandardCharsets.UTF_8);
        }
        return null;
    }


}
