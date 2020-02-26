package xyz.erupt.tpl.service;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.java.Log;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedCaseInsensitiveMap;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import xyz.erupt.annotation.sub_erupt.Html;
import xyz.erupt.core.config.EruptConfig;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.tpl.annotation.EruptTpl;
import xyz.erupt.tpl.annotation.TplAction;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author liyuepeng
 * @date 2020-02-24
 */
@Order(1)
@Service
@Log
public class TplService implements ApplicationRunner {

    private final Map<String, Class> tplActions = new LinkedCaseInsensitiveMap<>();
    @Autowired
    private EruptConfig eruptConfig;
    @Autowired
    private TemplateEngine templateEngine;
    @Autowired
    private Configuration freemarkerConfig;

    public Class getAction(String name) {
        return tplActions.get(name);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        EruptSpringUtil.scannerPackage(eruptConfig.getScannerPackage(), new TypeFilter[]{
                new AnnotationTypeFilter(EruptTpl.class)
        }, clazz -> {
            for (Method method : clazz.getDeclaredMethods()) {
                TplAction tplAction = method.getAnnotation(TplAction.class);
                if (null != tplAction) {
                    tplActions.put(tplAction.value(), clazz);
                }
            }
        });
        log.info("Erupt tpl initialization complete");
    }

    public void execFreeMarkerTpl(String name, Map<String, Object> data, HttpServletResponse response) throws IOException, TemplateException {
        freemarkerConfig.setDirectoryForTemplateLoading(new ClassPathResource("tpl").getFile());
        Template template = freemarkerConfig.getTemplate(name, "utf-8");
        template.process(data, response.getWriter());
    }


    public String execThymeleafTpl(Html html) {
        try {
            Resource resource = new ClassPathResource(html.path());
            String template = FileUtils.readFileToString(resource.getFile());
            if (!html.htmlHandler().isInterface()) {
                Context ctx = new Context();
                ctx.setVariables(html.htmlHandler().newInstance().getData(html.params()));
                return templateEngine.process(template, ctx);
            } else {
                return template;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
