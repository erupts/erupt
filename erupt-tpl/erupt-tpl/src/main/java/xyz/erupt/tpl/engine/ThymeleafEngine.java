package xyz.erupt.tpl.engine;

import lombok.SneakyThrows;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import xyz.erupt.annotation.sub_erupt.Tpl;

import java.io.Writer;
import java.util.Map;

/**
 * @author YuePeng
 * date 2021/1/16 14:39
 */
public class ThymeleafEngine extends EngineTemplate<TemplateEngine> {

    @Override
    public Tpl.Engine engine() {
        return Tpl.Engine.Thymeleaf;
    }

    @Override
    public TemplateEngine init() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setCacheable(false);
        resolver.setPrefix("/");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCheckExistence(true);
        resolver.setUseDecoupledLogic(true);
        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.addTemplateResolver(resolver);
        return templateEngine;
    }

    @SneakyThrows
    @Override
    public void render(TemplateEngine templateEngine, String filePath, Map<String, Object> bindingMap, Writer out) {
        Context ctx = new Context();
        ctx.setVariables(bindingMap);
        out.write(templateEngine.process(filePath, ctx));
    }
}
