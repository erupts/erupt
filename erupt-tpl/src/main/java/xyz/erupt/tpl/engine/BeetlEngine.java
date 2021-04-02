package xyz.erupt.tpl.engine;

import lombok.SneakyThrows;
import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.Template;
import org.beetl.core.resource.ClasspathResourceLoader;
import xyz.erupt.annotation.sub_erupt.Tpl;

import java.io.Writer;
import java.util.Map;

/**
 * @author YuePeng
 * date 2021/4/1 22:56
 */
public class BeetlEngine extends EngineTemplate<GroupTemplate> {

    @Override
    public Tpl.Engine engine() {
        return Tpl.Engine.Beetl;
    }

    @SneakyThrows
    @Override
    public GroupTemplate init() {
        ClasspathResourceLoader resourceLoader = new ClasspathResourceLoader("/");
        Configuration cfg = Configuration.defaultConfiguration();
        return new GroupTemplate(resourceLoader, cfg);
    }

    @Override
    public void render(GroupTemplate groupTemplate, String filePath, Map<String, Object> bindingMap, Writer out) {
        Template template = groupTemplate.getTemplate(filePath);
        template.binding(bindingMap);
        template.renderTo(out);
    }
}
